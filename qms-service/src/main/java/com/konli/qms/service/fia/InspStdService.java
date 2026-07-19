package com.konli.qms.service.fia;

import com.konli.qms.domain.fia.entity.FiaInspStd;
import com.konli.qms.domain.fia.entity.FiaInspStdItem;
import com.konli.qms.service.fia.dto.InspStdVo;

import java.util.List;

public interface InspStdService {

    List<FiaInspStd> list();

    InspStdVo get(String id);

    FiaInspStd create(FiaInspStd std, List<FiaInspStdItem> items);

    void update(FiaInspStd std);

    void delete(String id);
}
