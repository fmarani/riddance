
import scala.xml._

val x = <riddance req="mailout">
<template>
    <text>testestest</text>
    <html><![CDATA[<html><body>ayo!</body></html>]]></html>
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


def getTemplateMap(xmlseq: NodeSeq) = {
    val attrList = xmlseq map (x => Map((x\"@k").text -> (x\"@v").text))
    attrList reduceLeft ((x:Map[String,String],y:Map[String,String]) => x++y)
}

def getItemsMap(items: NodeSeq) = items.toList map (item => getTemplateMap(item\"map") )

def getTemplateBlocks(blocks: NodeSeq) = blocks.toList map (block => (block\"@name", getItemsMap(block\"item"))) 

val tt = (x\"template"\"text").text
val th = (x\"template"\"html").text

val tupleD = (x\"iterations"\"unit" map (unit => (
 (unit\"@email").text,
 getTemplateMap(unit\"map"),
 getTemplateBlocks(unit\"block")
)) ).toList

println(tt, th, tupleD)

case class RiddanceData (
    val templateText: String, 
    val templateHtml: String, 
    val subject: String,
    val data: List[ (String,Map[String,String],Map[String,List[Map[String,String]]]) ] 
) 



