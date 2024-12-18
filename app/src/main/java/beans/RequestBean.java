package beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.Data;

@ManagedBean(name = "requestBean", eager = true)
@SessionScoped
@Data
public class RequestBean {



    public RequestData getRequestData() {

    }
}
