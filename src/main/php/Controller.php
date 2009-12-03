<?php

class riddance_UnitOfWork
{
    /**
     * @var string
     */
    public $templateText;

    /**
     * @var string
     */
    public $templateHtml;
    
    /**
     * @var string
     */
    public $subject;
    
    /**
     * @var array
     */
    public $emails;
    
    /**
     * @var array
     */
    public $templateVars;
}


class riddance_Controller
{
    private $client;
    private $queue;

    public function connect()
    {
        $c = new messaging_Client();
        $q = messaging_destination_Queue::create('riddance-in');

        $c->connect();
        $this->client = $c;
        $this->queue = $q;
    }

    public function disconnect()
    {
        $c->disconnect();
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
            array_push($blockMapContainers, array_map($this->flatten_array, $blockMapContainer));
            array_push($templateMaps, $templateMap);
        }

        //{\"template-text\" : \"bar\", \"template-html\" : \"bar\", \"email\" : \"bar@bar.com\", \"blkdata\" : \"[{\"aaa\" : \"AAA\"}]\", \"data\" : \"{\"zzz\" : \"ZZZ\"}\"}
        $prepared = array(
          "template-text" => $unitOfWork->templateText,
          "template-html" => $unitOfWork->templateHtml,
          "subject" => $unitOfWork->subject,
          "emails" => $unitOfWork->emails,
          "blkdata" => $blockMapContainers,
          "data" => $templateMaps
        );
        return $prepared;
    }

    public function release(riddance_UnitOfWork $unitOfWork)
    {
        // CHUNKIFY UNITOFWORK!! (max 300 emails per msg)
        $content = $this->prepareForSubmission($unitOfWork);
	    $envelope = new messaging_message_JsonMessage($content);

        $this->client->send($this->queue, $envelope);
    }
}
