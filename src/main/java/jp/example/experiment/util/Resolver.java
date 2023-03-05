package jp.example.experiment.util;

public class Resolver {
	
    private static final char NESTED        = '.';
    private static final char MAPPED_START  = '(';
    private static final char MAPPED_END    = ')';
    private static final char INDEXED_START = '[';
    private static final char INDEXED_END   = ']';

    public boolean hasNested(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return false;
        }
        return remove(expression) != null;
    }
	
	
    
    public String next(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        boolean indexed = false;
        boolean mapped  = false;
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (indexed) {
                if (c == INDEXED_END) {
                    return expression.substring(0, i + 1);
                }
            } else if (mapped) {
                if (c == MAPPED_END) {
                    return expression.substring(0, i + 1);
                }
            } else {
                switch (c) {
                case NESTED:
                    return expression.substring(0, i);
                case MAPPED_START:
                    mapped = true;
                    break;
                case INDEXED_START:
                    indexed = true;
                    break;
                default:
                    break;
                }
            }
        }
        return expression;
    }
    
    
    
    public String remove(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        final String property = next(expression);
        if (expression.length() == property.length()) {
            return null;
        }
        int start = property.length();
        if (expression.charAt(start) == NESTED) {
            start++;
        }
        return expression.substring(start);
    }
    
    
    
    public String getProperty(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if ((c == NESTED) || (c == MAPPED_START || c == INDEXED_START)) {
                return expression.substring(0, i);
            }
        }
        return expression;
    }
    
    
    
    public int getIndex(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (c == NESTED || c == MAPPED_START) {
                return -1;
            }
            if (c == INDEXED_START) {
                final int end = expression.indexOf(INDEXED_END, i);
                if (end < 0) {
                    throw new IllegalArgumentException("Missing End Delimiter");
                }
                final String value = expression.substring(i + 1, end);
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("No Index Value");
                }
                int index = 0;
                try {
                    index = Integer.parseInt(value, 10);
                } catch (final Exception e) {
                    throw new IllegalArgumentException("Invalid index value '"
                            + value + "'");
                }
                return index;
            }
        }
        return -1;
    }
    
    
    
    public String getKey(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (c == NESTED || c == INDEXED_START) {
                return null;
            }
            if (c == MAPPED_START) {
                final int end = expression.indexOf(MAPPED_END, i);
                if (end < 0) {
                    throw new IllegalArgumentException("Missing End Delimiter");
                }
                return expression.substring(i + 1, end);
            }
        }
        return null;
    }
    
    
    
    public boolean isIndexed(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return false;
        }
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (c == NESTED || c == MAPPED_START) {
                return false;
            }
            if (c == INDEXED_START) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public boolean isMapped(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return false;
        }
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (c == NESTED || c == INDEXED_START) {
                return false;
            }
            if (c == MAPPED_START) {
                return true;
            }
        }
        return false;
    }
}
