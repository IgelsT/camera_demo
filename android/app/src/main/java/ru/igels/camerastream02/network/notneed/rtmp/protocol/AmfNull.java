package ru.igels.camerastream02.network.notneed.rtmp.protocol;

/**
 * Object that represents an AMF0 null value parameter
 *
 * @author Benoit LETONDOR
 */
public final class AmfNull
{
    @Override
    public boolean equals(Object o)
    {
        return o != null && (o instanceof AmfNull);
    }
}
