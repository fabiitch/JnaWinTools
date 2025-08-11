package com.nz.structure;

import com.sun.jna.Structure;

@Structure.FieldOrder({
    "dmDeviceName", "dmSpecVersion", "dmDriverVersion", "dmSize", "dmDriverExtra",
    "dmFields", "dmPositionX", "dmPositionY", "dmDisplayOrientation",
    "dmDisplayFixedOutput", "dmColor", "dmDuplex", "dmYResolution", "dmTTOption",
    "dmCollate", "dmFormName", "dmLogPixels", "dmBitsPerPel", "dmPelsWidth", "dmPelsHeight",
    "dmDisplayFlags", "dmDisplayFrequency"
})
public class DEVMODE extends Structure {
    public byte[] dmDeviceName = new byte[32];
    public short dmSpecVersion;
    public short dmDriverVersion;
    public short dmSize;
    public short dmDriverExtra;
    public int dmFields;

    public int dmPositionX;
    public int dmPositionY;
    public int dmDisplayOrientation;
    public int dmDisplayFixedOutput;

    public short dmColor;
    public short dmDuplex;
    public short dmYResolution;
    public short dmTTOption;
    public short dmCollate;

    public byte[] dmFormName = new byte[32];
    public short dmLogPixels;
    public int dmBitsPerPel;
    public int dmPelsWidth;
    public int dmPelsHeight;
    public int dmDisplayFlags;
    public int dmDisplayFrequency;
}
