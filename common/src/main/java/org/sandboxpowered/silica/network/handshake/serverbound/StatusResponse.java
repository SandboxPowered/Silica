package org.sandboxpowered.silica.network.handshake.serverbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class StatusResponse implements Packet {
    private String responseJson = """
            {
                "version": {
                    "name": "1.17.1",
                    "protocol": 756
                },
                "players": {
                    "max": 100000,
                    "online": 58242,
                    "sample": [
                    ]
                },
                "description": {
                    "text": "A Sandbox Silica Server"
                },
                "favicon": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAACxklEQVR4nO2av24TQRDGv7NP8Z8oSiQcR5cGB11JQ0EKKHgARKQg0SAeAQlegJI+Lc8QCaQg6CidmhpFSkhnJ0hRXES2Yplqw3nji+92Zm4Oeb/uzr692d/N7cy3dhBF0QQLrIp2ANryALQD0JYHoB2AtjwA7QC05QFoB6AtD0A7AG2FRd6sG8dO1z09OmKO5J8CSTPUDkN86XTYx+UEIgLgoNPBvVA+uThAsK8B3TguZPIA8H5lhTwGK4APa2ucwxUi1kc1GI+x3+vdHL/a2OAcXkSiuZqEAfACscd2VaFlkBvI3mBAuh4QqAJPajVsLy3lvu7F+jrqlWxLkgHJAYCcAaa5MSXpcDjE4XB483nWlfrr2dnU8bzs+HV9nSfMVJEzIK27S6vRrqUrCWS/12N5+gARwF4U4fHy8tzv/R6N8Pr09Nb5EMBbRyClAPBpcxMPm83c11Gzg2vyAHEN+HFx4QTAfm0MEHtiHJ3ePBVaBtOUBwjn0wdKAsBWEsjO8TH+jMcAeFPfiASAqxTdpYOtralj7r0Bkhn6ORqxtaRZ1Y1j542VWWJxg0VDANx3l2yRG6FZK/XzVgvNapUybGZRXwnyIng1maARBFPnvp2fTx2X2RazmKG89ZobCCUL2NxgAOCdQ+Nyv17H9uoq6d6lAGCLw/RkVSkAmJ1gSReYplIA0LDF8+6RReIAkto9OUF/Rve402jggcNWuoGhDoD7J6+82UHxCKpmqAy2WGxLjKrvl5f42O/fOt+qVPDG2oWiZEBpAdi663VZCAC2uGwxyQ1qTZ7TfZIAaNhg7nuSq4AJ6GW7jarlCjklBZutDH62VmwOx1dEhon1AZLBc26OkqvAs1oNjxx+DHWRxK4wqx2W6tgkJm4k+i8xChDJSSclCuB/0ML/U9QD0A5AWx6AdgDa8gC0A9CWB6AdgLY8AO0AtPUXrV7219gkQOMAAAAASUVORK5CYII="
            }""";

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
