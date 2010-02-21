<?php
/**
 * @package riddance
 * @copyright 2010 Tangent Labs
 * @version SVN: $Id: Client.php 1589 2010-02-21 01:23:35Z maranif $
 */

/**
 * Riddance unit of work
 * 
 * @package riddance
 */
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


