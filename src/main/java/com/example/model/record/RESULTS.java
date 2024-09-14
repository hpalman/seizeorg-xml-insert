package com.example.model.record;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RESULTS")
public class RESULTS {
    private List<ROW> rows;

    @XmlElement(name = "ROW")
    public List<ROW> getRows() {
        return rows;
    }

    public void setRows(List<ROW> rows) {
        this.rows = rows;
    }
}

