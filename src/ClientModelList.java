import java.util.ArrayList;

public class ClientModelList extends ArrayList<ClientModel> {
    public int getItemIndexByPort(int port) {
        for (ClientModel client : this) {
            if (client.port() == port) {
                return indexOf(client);
            }
        }
        return port;
    }
}
