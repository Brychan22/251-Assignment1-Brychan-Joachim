package Syntax;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import Parsing.StringParser;

import java.awt.Color;

public class JavaHighlighter implements Highlighter{
    // Keep track of all identified points
    Map<Integer, Integer> keywordPoints = new HashMap<>();
    Map<Integer, Integer> stringPoints = new HashMap<>();
    Map<Integer, Integer> numberPoints = new HashMap<>();
    Map<Integer, Integer> charPoints = new HashMap<>();
    Map<Integer, Integer> classPoints = new HashMap<>();
    Map<Integer, Integer> commentPoints = new HashMap<>();

    StringParser parser;
    String source;
    public JavaHighlighter(String source){
        parser = new StringParser(source.replace("\r\n", "\n")); // Ensure newlines only count as one character
        this.source = source;
    }

    static String[] Keywords = new String[]{"public", "private", "protected", "package", "static", "implements", "extends", "true", "false", "import", "final", "class", "if", "else", "return", "switch", "case", "null"};
    static String[] Classes = new String[]{"String", "File", "int", "Integer", "char", "boolean", "void", "Map", "double", "float", "byte", "Exception", "Map"};

    public void highlightSymbols(StyledDocument doc){
        int next =0;
        try{
            charloop:
        while(parser.move()){
            if (parser.current() == '/' && parser.peek() == '/'){
                // line comment
                int endpos = parser.findNext("\n");
                commentPoints.put(parser.getPostition(), endpos);
                parser.move(endpos-parser.getPostition());
            }
            if (parser.current() == '/' && parser.peek() == '*'){
                // block comment
                int endpos = parser.findNext("*/");
                commentPoints.put(parser.getPostition(), endpos+2);
                parser.move(endpos-parser.getPostition());
            }
            for (String keyword : Keywords) {
                if(parser.atWord(keyword)){
                    if ((parser.behind() == '\n' || parser.behind() == ' ' || parser.behind() == 0) && parser.peek(keyword.length()) == ' ' ){
                        keywordPoints.put(parser.getPostition(), parser.getPostition() + keyword.length());
                        parser.move(keyword.length());
                        //continue charloop; // skip to next character iteration
                    }
                }
            }
            for (String sClass : Classes) {
                if(parser.atWord(sClass)){
                    if (((parser.behind() == '\n' || parser.behind() == ' ' || parser.behind() == 0 || parser.behind() == '<') // Begins with \n, start of file, '<' or ' '
                    && (parser.peek(sClass.length()) == ' ' || parser.peek(sClass.length()) == '[')) || // Ends with ' ' or [
                    (parser.behind() == '(' && parser.peek(sClass.length()) == ')')) // Case that we're casting eg. (byte)
                    {
                        classPoints.put(parser.getPostition(), parser.getPostition() + sClass.length());
                        parser.move(sClass.length());
                        //continue charloop; // skip to next character iteration
                    }
                }
            }
            if (parser.current() == '\"'){
                int startPos = parser.getPostition();
                //int next = parser.findNext("\"");
                next = parser.findNext("\"");
                if (next == -1){
                    int i = 0;
                }
                while(!parser.isEnd(0) && source.substring(parser.getPostition(), next).endsWith("\\\"")){ // handle the escaped quotation mark
                    parser.move(next-parser.getPostition());
                    next = parser.findNext("\"");
                    if (next == -1) {
                        next = parser.getPostition();
                        //break;
                    }
                }
                stringPoints.put(startPos, next+1);
                parser.move(next-startPos);
            }

            if (parser.current() >= '0' && parser.current() <= '9' && (parser.behind() == ' ' || parser.behind() == '(' || parser.behind() == ')' || parser.behind() == '[')){
                boolean hex = false;
                if (parser.peek() == 'x' && (parser.peek(2) >= '0'|| parser.peek(2) >= 'A') && (parser.peek(2) <= '9' || parser.peek(2) <= 'F')){
                    hex = true;
                }
                // Numbers
                int startPos = parser.getPostition();
                while((parser.peek() >= '0' && parser.peek() <= '9' ) || hex && ((parser.peek() >= 'A' && parser.peek() <= 'F') || parser.peek() == 'x')){
                    parser.move();
                }
                numberPoints.put(startPos, parser.getPostition()+1);
            }
        }

        SimpleAttributeSet keywordsAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordsAttributeSet, Color.BLUE);
        for (Integer key : keywordPoints.keySet()) {
            doc.setCharacterAttributes(key, keywordPoints.get(key)-key, keywordsAttributeSet, false);  
        }
        SimpleAttributeSet stringAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttributeSet, Color.ORANGE);
        for (Integer key : stringPoints.keySet()) {
            doc.setCharacterAttributes(key, stringPoints.get(key)-key, stringAttributeSet, false);  
        }
        SimpleAttributeSet numberAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(numberAttributeSet, Color.GRAY);
        for (Integer key : numberPoints.keySet()) {
            doc.setCharacterAttributes(key, numberPoints.get(key)-key, numberAttributeSet, false);  
        }
        SimpleAttributeSet classAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(classAttributeSet, Color.RED);
        for (Integer key : classPoints.keySet()) {
            doc.setCharacterAttributes(key, classPoints.get(key)-key, classAttributeSet, false);  
        }
        SimpleAttributeSet commentAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttributeSet, Color.GREEN);
        for (Integer key : commentPoints.keySet()) {
            doc.setCharacterAttributes(key, commentPoints.get(key)-key, commentAttributeSet, false);  
        }
        }
        catch (IndexOutOfBoundsException e){
            int i = 0;
        }
        
    }    
}
