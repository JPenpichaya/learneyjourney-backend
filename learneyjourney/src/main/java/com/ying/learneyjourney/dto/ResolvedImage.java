package com.ying.learneyjourney.dto;

public class ResolvedImage {
    private final String src;
    private final String alt;
    private final String provider;
    private final boolean generated;

    public ResolvedImage(String src, String alt, String provider, boolean generated) {
        this.src = src;
        this.alt = alt;
        this.provider = provider;
        this.generated = generated;
    }

    public String getSrc() { return src; }
    public String getAlt() { return alt; }
    public String getProvider() { return provider; }
    public boolean isGenerated() { return generated; }
}