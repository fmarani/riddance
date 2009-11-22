//#!/bin/sh
//exec scala "$0" "$@"
//!#


object TemplateEngine {
 import scala.collection.mutable.ListBuffer

 val reVar = """\{([a-zA-Z0-9]+)\}""".r

 def substVars(templateChunk: String, templateMap: Map[String,String]) = {
	var lastPos = 0
	var buf = new ListBuffer[String]
	reVar.findAllIn(templateChunk).matchData.foreach { m =>
		val key = m.group(1)
		if(lastPos != m.start) buf += templateChunk.substring(lastPos, m.start)
		buf += templateMap.getOrElse(key,"")
		lastPos = m.end
	}
	if(lastPos != templateChunk.length) buf += templateChunk.substring(lastPos, templateChunk.length)
	buf.mkString
 }

 val reBlock = """(?s)<!-- BEGIN DYNAMIC BLOCK: ([a-zA-Z0-9]+) -->(.*?)<!-- END DYNAMIC BLOCK: \1 -->""".r

 def iterBlocks(templateChunk: String, blockMaps: Map[String,List[Map[String,String]]], templateMap: Map[String,String]): String = {
	val matches = reBlock.findAllIn(templateChunk).matchData
	if(!matches.hasNext) {
		// evaluate last level of template
		substVars(templateChunk, templateMap)
	} else {
		var lastPos = 0
		var buf = new ListBuffer[String]
		matches.foreach { m =>
			val blockName = m.group(1)
			val blockContent = m.group(2)
			// append text before the match
			if(lastPos != m.start) buf += templateChunk.substring(lastPos, m.start)

			// if block refers to unexisting entry, skip it
			if(blockMaps.contains(blockName)) {
				// elaborate block by recursively call this function
				blockMaps(blockName).foreach( blockMap =>
					buf += iterBlocks(blockContent, blockMaps, blockMap)
				)
			}

			// save position of last match
			lastPos = m.end
		}

		// append text after last match
		if(lastPos != templateChunk.length) buf += templateChunk.substring(lastPos, templateChunk.length)

		// evaluate first level of template
		substVars(buf.mkString, templateMap)
	} 
 }
 def render = iterBlocks _
}

object Template extends Application {

	val template = scala.io.Source.fromFile("template.tpl").getLines.mkString
	val subexample = List(
	 Map("Id" -> "12", "Data" -> "Lots of data"),
	 Map("Id" -> "34", "Data" -> "Even more data") )
	val example = Map("Name" -> "John")
	val exampleBlocks = Map("TestBlock" -> subexample)
	
	println(TemplateEngine.render(template, exampleBlocks, example))
}

//Template
