package house.greenhouse.greenhouseconfig.api.lang;

/**
 * A value that can have a comment attached to it.
 * <p>
 * This is designed to be implemented by language implementations.
 */
public interface CommentedValue {
    /**
     * Makes a copy of this commented value with the given comment.
     *
     * @param comments the comments the resulting value should have attached.
     * @return the new value with comments attached.
     */
    CommentedValue withComment(String[] comments);
}
