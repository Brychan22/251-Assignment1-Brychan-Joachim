package Syntax;

import java.util.HashMap;
import java.util.Map;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import Parsing.StringParser;

import java.awt.Color;

public class JavaHighlighter{
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

    //
    static String[] Keywords = new String[]{"abstract", "assert", "break", "case", "catch", "class", "const", "continue", "do", "else", "enum", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "instanceof", "interface", "native", "new", "package", "private", "protected", "public", "return", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};
    // values that are usually drawn as keywords
    static String[] keywordData = new String[]{"true", "false", "null"};
    // base class types
    static String[] Classes = new String[]{"String", "int", "Integer", "char", "Character", "boolean", "Boolean", "double", "Double", "float", "Float", "byte", "Byte", "Exception"};

    /**
     * Matches an expression from the current position if it matches search, through to end, ignoring ignore
     * @param search
     * @param ignore
     * @param end
     * @param target
     */
    private void SearchSymbolBounds(String search, String ignore, String end, Map<Integer, Integer> target){
        if(parser.atWord(search)){
            int startPos = parser.getPostition();
            int endPos = parser.findNext(end != null ? end : search);
            parser.move(endPos-startPos);
            if (ignore != null){
                while (parser.inWord(ignore)){
                    parser.move(endPos-startPos);
                    endPos = parser.findNext(end != null ? end : search);
                }
            }
            target.put(startPos, endPos + (end != null ? end.length() : search.length()));            
        }
    }

    private void searchWordBounds(String word, char[] acceptedStarts, char[] acceptedEnds, boolean strict, Map<Integer, Integer> target){
        if (parser.atWord(word)){
            int startCharIndex = -1;
            int endCharIndex = -1;
            for (int i = 0; i < acceptedStarts.length; i++) {
                if(acceptedStarts[i] == parser.behind()){
                    startCharIndex = i;
                    break;
                }
            }
            for (int i = 0; i < acceptedEnds.length; i++)  {
                if(acceptedEnds[i] == parser.peek(word.length())){
                    endCharIndex = i;
                    break;
                }
            }
            if (strict && (acceptedEnds != acceptedStarts)){
                return;
            }
            else if (startCharIndex >= 0 && endCharIndex >= 0){
                target.put(parser.getPostition(), parser.getPostition() + word.length());
                parser.move(word.length());
            }
            
        }
    }

    private void searchNumber(Map<Integer, Integer> target){
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
            target.put(startPos, parser.getPostition()+1);
        }
    }


    public void highlightSymbols(StyledDocument doc){
        while (parser.move()){
            // Find comments first
            SearchSymbolBounds("//", null, "\n", commentPoints);
            SearchSymbolBounds("/*", null, "*/", commentPoints);
            // Find strings
            SearchSymbolBounds("\"", "\\\"", "\"", stringPoints);
            // Find classes
            for (String string : Classes) {
                searchWordBounds(string, new char[]{' ', '(', '<', '\n', 0, '['}, new char[]{' ', ')', '>', ',', '.', '}', '[', ']'}, false, classPoints);
            }
            // Find keywords
            for (String string : Keywords){
                searchWordBounds(string, new char[]{' ', '\n', 0, '{','('}, new char[]{' ', '{', '('}, false, keywordPoints);
            }
            // Find data that are normally highlighted like keywords
            for (String string : keywordData){
                searchWordBounds(string, new char[]{' ', '=', '<', '('}, new char[]{' ', ';', ',', '>', ')'}, false, keywordPoints);
            }
            // Search numbers
            searchNumber(numberPoints);
        }

        SimpleAttributeSet keywordsAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordsAttributeSet, new Color(0x1080FF));
        for (Integer key : keywordPoints.keySet()) {
            doc.setCharacterAttributes(key, keywordPoints.get(key)-key, keywordsAttributeSet, false);  
        }
        SimpleAttributeSet stringAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(stringAttributeSet, new Color(0xA62817));
        for (Integer key : stringPoints.keySet()) {
            doc.setCharacterAttributes(key, stringPoints.get(key)-key, stringAttributeSet, false);  
        }
        SimpleAttributeSet numberAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(numberAttributeSet, new Color(0x787813));
        for (Integer key : numberPoints.keySet()) {
            doc.setCharacterAttributes(key, numberPoints.get(key)-key, numberAttributeSet, false);  
        }
        SimpleAttributeSet classAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(classAttributeSet, new Color(0x2395C2));
        for (Integer key : classPoints.keySet()) {
            doc.setCharacterAttributes(key, classPoints.get(key)-key, classAttributeSet, false);  
        }
        SimpleAttributeSet commentAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttributeSet, new Color(0x207513));
        for (Integer key : commentPoints.keySet()) {
            doc.setCharacterAttributes(key, commentPoints.get(key)-key, commentAttributeSet, false);  
        }
        
    }    
}
