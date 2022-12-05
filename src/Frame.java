/**
 * Container for a message, with extra information.
 * Using a frame, we can send messages that contain information about the type of message it is and the user who sent the message.
 */
public class Frame {

    public final int tag;
    public final byte[] data;

    /**
     * Creates a <code>Frame</code> containing the specified information.
     * @param tag this frame's type.
     * @param data the message this frame contains.
     */
    public Frame(int tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }
}

