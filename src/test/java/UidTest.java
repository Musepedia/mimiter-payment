import com.github.wujun234.uid.UidGenerator;
import com.github.wujun234.uid.impl.DefaultUidGenerator;
import com.mimiter.payment.App;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
public class UidTest {

    @Resource(name = "defaultUidGenerator")
    UidGenerator uidGenerator;

    @Test
    public void test(){
        System.out.println(uidGenerator.getUID());
    }
}
