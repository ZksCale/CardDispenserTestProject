
import com.fazecast.jSerialComm.SerialPort;
import com.wellhoo.carddispensertestproject.MachineOrder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class testUnit {
    Logger log = LoggerFactory.getLogger(testUnit.class);

    @Test
    public void t0() throws Exception {
        byte[] msg = new byte[200];

//        int len = MachineOrder.initializeInfo(msg, '0');
        int len=MachineOrder.negativeResponse(msg,'0');
        for (int i = 0; i < len; i++) {
            System.out.print((char)msg[i]+", ");
        }
        System.out.println();
        System.out.println(Arrays.toString(msg));


    }
}
