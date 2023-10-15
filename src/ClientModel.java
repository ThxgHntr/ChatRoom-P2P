import java.net.InetAddress;

public record ClientModel(InetAddress ip, int port) {

    @Override
    public String toString() {
        // split ip with / and get the last part
        String[] split = ip.toString().split("/");
        return (split[split.length - 1] + ":" + port).trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ClientModel other = (ClientModel) obj;

        return ip.equals(other.ip) && port == other.port;
    }
}