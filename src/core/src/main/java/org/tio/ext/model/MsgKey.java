package org.tio.ext.model;


import java.util.Objects;

public class MsgKey {

    private MsgType type;

    private String channel;

    private String actualChannel;

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getActualChannel() {
        return actualChannel;
    }

    public void setActualChannel(String actualChannel) {
        this.actualChannel = actualChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgKey msgKey = (MsgKey) o;
        return type == msgKey.type &&
                Objects.equals(channel, msgKey.channel) &&
                Objects.equals(actualChannel, msgKey.actualChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, channel, actualChannel);
    }
}
