<?php

class riddance_UnitOfWork
{
    public $templateText;
    public $templateHtml;
    public array $emails;
    public array $templateVars;
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

    protected function prepareForSubmission($unitOfWork)
    {
        // prepare data
        //{\"template-text\" : \"bar\", \"template-html\" : \"bar\", \"email\" : \"bar@bar.com\", \"blkdata\" : \"[{\"aaa\" : \"AAA\"}]\", \"data\" : \"{\"zzz\" : \"ZZZ\"}\"}

    }

    public function release($unitOfWork)
    {
        $content = $this->prepareForSubmission($unitOfWork);
	    $envelope = new messaging_message_JsonMessage($content);

        $this->client->send($this->queue, $envelope);
    }
}
