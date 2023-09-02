package team.unnamed.creativeglyphs.map;

public final class GlyphMapOptions {

    public static final GlyphMapOptions DEFAULT = GlyphMapOptions.builder().build();

    private final boolean ignoreCase;
    private final boolean ignoreOverlaps;

    private GlyphMapOptions(
            boolean ignoreCase,
            boolean ignoreOverlaps
    ) {
        this.ignoreCase = ignoreCase;
        this.ignoreOverlaps = ignoreOverlaps;
    }

    public boolean ignoreCase() {
        return ignoreCase;
    }

    public boolean ignoreOverlaps() {
        return ignoreOverlaps;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean ignoreCase = true;
        private boolean ignoreOverlaps = true;

        private Builder() {
        }

        public boolean isIgnoreCase() {
            return ignoreCase;
        }

        public Builder ignoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public Builder ignoreCase() {
            this.ignoreCase = true;
            return this;
        }

        public boolean isIgnoreOverlaps() {
            return ignoreOverlaps;
        }

        public Builder ignoreOverlaps(boolean ignoreOverlaps) {
            this.ignoreOverlaps = ignoreOverlaps;
            return this;
        }

        public Builder ignoreOverlaps() {
            this.ignoreOverlaps = true;
            return this;
        }

        public GlyphMapOptions build() {
            return new GlyphMapOptions(ignoreCase, ignoreOverlaps);
        }

    }

}
