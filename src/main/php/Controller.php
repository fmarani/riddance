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

    protected function prepareForSubmission($unitOfWork)
    {
        // prepare data
        
        // 1. extract first level of data in separate var
        
        // 2. create a map with all arrays in any level as first level
        
        //{\"template-text\" : \"bar\", \"template-html\" : \"bar\", \"email\" : \"bar@bar.com\", \"blkdata\" : \"[{\"aaa\" : \"AAA\"}]\", \"data\" : \"{\"zzz\" : \"ZZZ\"}\"}
        $prepared = array(
          "template-text" => $unitOfWork->templateText,
          "template-html" => $unitOfWork->templateHtml,
          "subject" => $unitOfWork->subject,
          "emails" => $unitOfWork->emails,
          // add
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
