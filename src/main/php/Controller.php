<?php

class riddance_Controller
{
    private $client;
    private $queue;

    public function connect()
    {
        $c = new messaging_Client();
        $q = messaging_destination_Factory::createQueue('riddance.in');

        $c->connect();
        $this->client = $c;
        $this->queue = $q;
    }

    public function disconnect()
    {
        $this->client->disconnect();
    }

    private function flatten_array($array) {
        $size=sizeof($array);
        $keys=array_keys($array);
        for($x = 0; $x < $size; $x++) {
            $element = $array[$keys[$x]];

            if(is_array($element)) {
                $results = $this->flatten_array($element);
                    $sr = sizeof($results);
                    $sk=array_keys($results);
                for($y = 0; $y < $sr; $y++) {
                    $flat_array[$sk[$y]] = $results[$sk[$y]];
                }
            } else {
                $flat_array[$keys[$x]] = strval($element);
            }
        }

        return $flat_array;
    }

    protected function prepareForSubmission($unitOfWork)
    {
        // prepare data
        $blockMapContainers = array();
        $templateMaps = array();
        // 1. extract first level of data in separate var
        // 2. create a map with all arrays in any level as first level
        foreach ($unitOfWork->templateVars as $mash) {
            $blockMapContainer = array();
            $templateMap = array();
            foreach ($mash as $key => $value) {
                if (is_array($value)) {
                    $blockMapContainer[$key] = $value;
                } else {
                    $templateMap[$key] = strval($value);
                }
            }
            array_push($blockMapContainers, array_map(array($this, "flatten_array"), $blockMapContainer));
            array_push($templateMaps, $templateMap);
        }

        if (count($unitOfWork->emails) != count($templateMaps))
            throw new RuntimeException("Number of emails and template contexts is different");

        // 3. create xml description of resulting structure
        $xw = new XMLWriter();
        $xw->openMemory();

        $xw->startElement("riddance");
        $xw->writeAttribute("req","mailout");

        $xw->startElement("template");
        $xw->writeElement("subject", $unitOfWork->subject);
        $xw->writeElement("text", $unitOfWork->templateText);
        $xw->startElement("html");
        $xw->writeCData($unitOfWork->templateHtml);
        $xw->endElement();
        $xw->endElement();
        
        $xw->startElement("iterations");
        for ($i = 0; $i < count($unitOfWork->emails); $i++) {
            $xw->startElement("unit");
            $xw->writeAttribute("email", $unitOfWork->emails[$i]);
            foreach ($templateMaps[$i] as $k => $v) {
                $xw->startElement("map");
                $xw->writeAttribute("k", $k);
                $xw->writeAttribute("v", $v);
                $xw->endElement();
            }
            foreach ($blockMapContainers[$i] as $blockName => $blockMapContainer) {
                $xw->startElement("block");
                $xw->writeAttribute("name", $blockName);
                foreach ($blockMapContainer as $blockMap) {
                    $xw->startElement("item");
                    foreach ($blockMap as $k => $v) {
                        $xw->startElement("map");
                        $xw->writeAttribute("k", $k);
                        $xw->writeAttribute("v", $v);
                        $xw->endElement();
                    }
                    $xw->endElement();
                }
                $xw->endElement();
            }
            $xw->endElement(); // unit
        }
        $xw->endElement(); // iterations
        $xw->endElement(); // riddance

        $xw->endDocument();
        return $xw->flush();
    }

    public function release(riddance_UnitOfWork $unitOfWork)
    {
        // CHUNKIFY UNITOFWORK!! (max 300 emails per msg)
        $content = $this->prepareForSubmission($unitOfWork);
	$envelope = new messaging_message_TextMessage($content);

        $this->client->send($this->queue, $envelope);
    }
}

