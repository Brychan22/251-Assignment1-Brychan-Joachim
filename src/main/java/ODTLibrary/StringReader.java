package ODTLibrary;

public class StringReader {
    int position;
    String source;
    // From https://stackoverflow.com/questions/2227921/simplest-way-to-get-a-complete-list-of-all-the-utf-8-whitespace-characters-in-ph
    char[] chars = new char[] {
        0x09,
        0x0A,
        0x0B,
        0x0C,
        0x0D,
        0x20,
        0x85,
        0xA0,
        0x1680,
        0x180E,
        0x2000,
        0x2001,
        0x2002,
        0x2003,
        0x2004,
        0x2005,
        0x2006,
        0x2007,
        0x2008,
        0x2009,
        0x200A,
        0x200B,
        0x200C,
        0x200D,
        0x2028,
        0x2029,
        0x202F,
        0x205F,
        0x2060,
        0x3000,
        0xFEFF
    };

    public int getUnread(){
        return source.length() - position;
    }

    public int getPosition(){
        return position;
    }

    public boolean isEnd(){
        return position >= source.length();
    }

    public char peek(){
        return source.charAt(position + 1);
    }

    public char peekAhead(int amount) {
        return source.charAt(position + amount);
    }

    public char current() {
        return source.charAt(position);
    }

    public void moveOne() {
        position++;
    }

    public void move(int amount){
        position += amount;
    }

    public void moveToNext(char c) {
        position = source.indexOf(c, position) != -1 ? source.indexOf(c, position) : source.length();
    }

    public void moveToNext(String s){
        position = source.indexOf(s, position) != -1 ? source.indexOf(s, position) : source.length();
    }

    public void moveToNextLine(){
        char c = current();
        while (c != '\r' && c != '\n' && !isEnd()){
            moveOne();
            c = current();
        }
    }

    public void skipWhitespace() {
        while (isWhitespace(current())){
            moveOne();
        }
    }

    public boolean isWhitespace(char c){
        boolean isWS = false;
        for (char d : chars) {
            if (c == d){
                isWS = true;
                break;
            }
        }
        return isWS;
    }

}