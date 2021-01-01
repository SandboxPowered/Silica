package org.sandboxpowered.silica.network.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class StatusResponse implements Packet {
    private String responseJson = "{\n" +
            "    \"version\": {\n" +
            "        \"name\": \"1.16.4\",\n" +
            "        \"protocol\": 754\n" +
            "    },\n" +
            "    \"players\": {\n" +
            "        \"max\": 100000,\n" +
            "        \"online\": 58242,\n" +
            "        \"sample\": [\n" +
            "        ]\n" +
            "    },\n" +
            "    \"description\": {\n" +
            "        \"text\": \"A Sandbox Silica Server\"\n" +
            "    },\n" +
            "    \"favicon\": \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAIEUExURcYsLMYtLctAQNRgYNt7e+GOjuScnOakpOenp+ekpOWenuKSkt2CgthtbdFTU8k4OMo6Ott5eeu1tfnn5//+/v////77+/fj4+y5ud+JidBSUscuLtBTU+qxsf339/jk5M9MTMxBQf/9/f79/eirq/vw8OSamtp0dOqvr/jm5v78/M1FRcYuLs9OTt6Ghu6/v/vy8u29vccyMtNdXeOWlvLPz/76+tZmZso5Oeelpfbe3vbf381ERNx9ffrs7N+Kitp2duCMjO/Fxfz29so7O/jn5+mvr9p3d8xAQMg0NNVjY/PU1Omtre/ExPTY2OWgoNZnZ8k2Nss+Ptlzc/HKyv34+PDJyeGQkNJXV8cvL+7Bwfvv792AgM5ISOiqqvfi4uipqdlxcco8PN2BgfPS0tRhYcgzM/z09O/Dw+28vPnq6uuzs85JSdJaWuSdney6utBRUccwMNJYWOGRkemsrNZoaOahofXZ2frr69p4eOqwsPno6M1HR89PT9+Hh+7AwNZpaccxMdReXuOXl9dra/Xb2/bd3ddsbMk5Odhubuempv35+fLNzeKVldNcXM1GRuu2tuy4uO2+vt6Fhc9NTdFUVOmurss/P/TX1+Wfn8g1NfDIyOGPj9FWVvrt7d1/f81ISM5KSuOYmPXa2vz19fLOztVkZOCNjeajo96EhNBPT8gyMqS3rBEAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAI1SURBVFhH7db7OxRRHAbwnSyhVmx7NqVWu9bKLotuKvckFYVCIbeQQonuF7GKSraQonSlK/9kzL6PmdmZM+f4fT4/zTnf9zszu+c8Z9dkMBhYhE0R5siozdExsVu2WuK2YZZXfIJ1u43I2Hck7tyVhCrT7j0O9Cklx+5FQpfT6kKDWoobIR2WVIQ1eZCiE9L2pSOswetDTFdGpj8rGx1hchBh23/g4KHD6JLJRZnPEcvRY4q1JHkZKPHLL7AWontVEWapirP8mQKu1yXFlRwvFftLT2CKyk2Irexk+SkMJafPRFV4SSVGdGfFBxHvuarqGkxJzl9g78Pa0A3W1NVfvNSAaX6N6IamxMvNqHBqQadMa1v7FVQ5dKBLyd7p6bqKBIMvBT0q3deKuI6D68hr6uktZ+7Evm6EKXqZb3HD3YqstpvI6RD6OvqbEFfz3kJM38Dg7do6tIS5gwhbzd179zUW5cGGtqfz4aPHaFz3BDVewtDTYcXSRKBAM2LOx5UkMPpMOt+eY5JCGCOu8ZgXLzGUxL+ayBPPN8ax2CU+hbyeDL4JYErytmCqkAxhQDEdusGa7Jl3s6rzreG9/io47eiG9A/FcyjxCaJRxjX/MfcTykwDqlUPcSx8/sK1gdrRoOXr9Lfvqq8k3ALCFLZhxmv8oP8xEDUyT8fqeuVvoUKPmfkJVs0uLmnfY9zPvRS+hLaKn2gLcfxaHOR5ukyg+bf5j+dv9ERV8J9leQWzBoOBwmT6D4RzjGary7mfAAAAAElFTkSuQmCC\"\n" +
            "}";

    public StatusResponse(String responseJson) {
        this.responseJson = responseJson;
    }

    public StatusResponse() {
    }

    @Override
    public void read(PacketByteBuf buf) {
        responseJson = buf.readString();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(responseJson);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
