package com.epam.microservice_resource_processor.utils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Mp3Parse {
    public Map<String, String> parseMP3(InputStream inputStream) {

        //detecting the file type
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        //Mp3 parser
        Mp3Parser Mp3Parser = new  Mp3Parser();
        try {
            Mp3Parser.parse(inputStream, handler, metadata, parseContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, String> resultMap = new HashMap<>();
        String[] metadataNames = metadata.names();
        for(String name : metadataNames) {
            resultMap.put(name, metadata.get(name));
        }
        return resultMap;
    }
}
