package Parsing;

public class StringParser {
    public StringParser(String source){
        this.source = source;
    }
    String source;
    int position = 0;

    public boolean isEnd(int change){
        if (position + change >= source.length()){
            return true;
        }
        else return false;
    }

    public void reset(){
        position = 0;
    }

    public char current(){
        return peek(0);
    }

    public int getPostition(){
        return position;
    }

    public char peek(){
        return peek(1);
    }

    public char peek(int amount){
        if (isEnd(amount)){
            return 0;
        }
        else{
            return source.charAt(position + amount);
        }
    }

    public char behind(){
        if (position == 0){
            return 0;
        }
        else return source.charAt(position-1);
    }

    public boolean move(){
        return move(1);
    }

    public boolean move(int amount) {
        if (isEnd(amount)) return false;
        else{
            position += amount;
            return true;
        }
    }

    public String consume(int amount) {
        if (isEnd(amount)){
            amount = source.length() - position;
        }
        String result = source.substring(position, amount+position);
        position += amount;
        return result;
    }

    public int findNext(String s){
        int subSearchIndex = 0;
        for (int i = 1; i < source.length() - position -1; i++) {
            if(subSearchIndex == s.length()){
                return position + i - subSearchIndex;
            }
            if (peek(i) == s.charAt(subSearchIndex++)){
                continue;
            }
            else{
                subSearchIndex = 0;
            }
        }
        return 0;
    }

    public boolean atWord(String s){
        if(s.length() + position > source.length()){
            return false;
        }
        if (source.substring(position, position + s.length()).equals(s)){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Takes a word and identifies if we're currently inside it
     * @param s the input word to search
     * @return true if the word matches
     */
    public boolean inWord(String s){
        char currentChar = current();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == currentChar){
                int oldPos = position;
                position -= i;
                boolean atW = atWord(s);
                position = oldPos;
                if (atW){
                    return true;
                }
            }
        }
        return false;
    }
    
}