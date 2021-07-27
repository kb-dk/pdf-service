package dk.kb.pdfservice.cachingtransformerfactory;

import com.google.common.base.Strings;

import javax.xml.transform.stream.StreamSource;

public class StreamSourceWrapper {

    private final StreamSource delegate;

    StreamSourceWrapper(StreamSource delegate) {
        this.delegate = delegate;
    }

    public boolean isCacheable() {
        return Strings.nullToEmpty(getDelegate().getSystemId()).length() > 0;
    }

    @Override
    public int hashCode() {
        if (isCacheable()) {
            return getDelegate().getSystemId().hashCode();
        } else {
            return getDelegate().hashCode();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StreamSourceWrapper)) {
            return false;
        }

        StreamSourceWrapper otherWrapper = (StreamSourceWrapper) other;
        if (isCacheable() && otherWrapper.isCacheable()) {
            return getDelegate().getSystemId().equals(otherWrapper.getDelegate().getSystemId());
        } else {
            return false;
        }
    }

    public StreamSource getDelegate() {
        return delegate;
    }
}
