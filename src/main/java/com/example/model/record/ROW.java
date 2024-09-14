package com.example.model.record;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ROW {
    private List<COLUMN> columns;

    @XmlElement(name = "COLUMN")
    public List<COLUMN> getColumns() {
        return columns;
    }

    public void setColumns(List<COLUMN> columns) {
        this.columns = columns;
    }
}

