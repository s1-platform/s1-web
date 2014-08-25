package test.formats;

import org.s1.web.formats.Beans;
import org.s1.web.formats.Maps;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Grigory Pykhov
 */
public class MapsTest {

    public static void main(String[] args) throws Exception{
        Map<String,Object> m = Maps.newSOHashMap(
                //"name2","asdf","i","12","name3","NAME","type","type2",
                "test2",Maps.newArrayList(
                        Maps.newSOHashMap("login","log","name","NAMEMM")
                ),
                "test22",Maps.newArrayList(
                Maps.newSOHashMap("login","log22","name","NAMEMM22"),
                        Maps.newSOHashMap("login","log32","name","NAMEMM23")
        ));
        Test1 t = Maps.convertMapToBean(Test1.class,m);
        //Beans.validate(t);
        System.out.println(Maps.convertBeanToMap(t));
    }

    public static class Test0{
        public String name2="qwe";

        private String name3="zxc";

        public int i=10;

        public String getName3() {
            return name3;
        }

        public void setName3(String name3) {
            this.name3 = name3;
        }
    }

    public static class Test1 extends Test0{

        public String name1="asd";

        private String name;

        @Future
        @NotNull
        private Date date = new Date();

        public List<Test2> test22;

        @Valid
        private List<Test2> test2;
        private Types type;
        public static enum Types{
            type1,type2
        }

        public Types getType() {
            return type;
        }

        public void setType(Types type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Test2> getTest2() {
            return test2;
        }

        public void setTest2(List<Test2> test2) {
            this.test2 = test2;
        }
    }

    public static class Test2{
        @Size(min = 10)
        private String name;
        private String login;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }

}
