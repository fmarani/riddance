
val x = <riddance req="mailout">
<template>
    <text>testestest</text>
    <html><![CDATA["<html><body>ayo!</body></html>"]]></html>
</template>
<iterations>
<unit email="flagzeta@gmail.com">
<map k="name" v="john"/>
<map k="surname" v="smith"/>
<block name="block1">
 <item>
 <map k="product" v="viagra pills"/>
 </item>
 <item>
 <map k="product" v="cialis gums"/>
 </item>
</block>
</unit>
<unit email="flagzeta@yahoo.it">
<map k="name" v="michael"/>
<map k="name" v="jordan"/>
<block name="block1">
 <item>
 <map k="product" v="basket balls"/>
 </item>
 <item>
 <map k="product" v="basket nets"/>
 </item>
</block>
</unit>
</iterations>
</riddance>

val tt = x\"template"\"text"
val th = x\"template"\"html"

def getTemplateMap(xmlseq: NodeSeq) = {
    val attrList = xmlseq map (x => Map((x\"@k").text -> (x\"@v").text))
    attrList reduceLeft ((x:Map[String,String],y:Map[String,String]) => x++y)
}

def getItemsMap(xmlseq: NodeSeq) = xmlseq map getTemplateMap(_)

def getTemplateBlocks(blocks: NodeSeq) = {
    blocks foreach (block => {
        val blockname = block\"@name"
        val items = getItemsMap(block\"item")

    val attrList = xmlseq map (x => Map((x\"@k").text -> (x\"@v").text))
    attrList reduceLeft ((x:Map[String,String],y:Map[String,String]) => x++y)
}


val tupleD = x\"iterations"\"unit" map (unit => (
 (unit\"@email").text,
 getTemplateMap(unit\"map"),
 getTemplateBlocks(unit\"block")

 val templateMap = unit\"map" reduceLeft ( m => println((m\"@k").text + "=" + (m\"@v").text) )
 unit\"block" foreach (b => println((b\"@name").text) )                  
}) 



case class RiddanceData (
    val templateText: String, 
    val templateHtml: String, 
    val subject: String,
    val data: List[ (String,Map[String,String],Map[String,List[Map[String,String]]]) ] 
) 



