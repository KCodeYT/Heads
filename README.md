![banner](./.github/images/banner.png)

What is this?
------------------------------

This project is a plugin for the nukkit server software environment.
This plugin adds player heads to the server.

How do I give myself a head?
------------------------------

    1. Open the chat in your client.
    2. Type /head followed by the name of the skull owner. 
       (This can be any player which has joined the server once
        or a java editiopn player)
    3. You will see a head appear in your inventory if the player was found.
       If the player was not found, you will see a message in the chat.

Developer API
------------------------------

```java
import cn.nukkit.Player;
import de.kcodeyt.heads.api.HeadAPI;

public class YourPlugin {

    public static void giveHeadByName(Player player, String name) {
        HeadAPI.giveHead(player, name).whenComplete((result, error) -> {
            if(result == null || error != null) {
                // something went wrong
                return;
            }

            if(!result.isSuccess()) {
                // use result.getCause() to find out why the head was not given
                return;
            }
            
            // head was given.
        });
    }

}
```