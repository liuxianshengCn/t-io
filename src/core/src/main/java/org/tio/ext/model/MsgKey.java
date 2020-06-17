package org.tio.ext.model;


import java.util.Objects;

public class MsgKey {

    private MsgType type;

    private String channel;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgKey msgKey = (MsgKey) o;
        return type == msgKey.type &&
                Objects.equals(channel, msgKey.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, channel);
    }
}
