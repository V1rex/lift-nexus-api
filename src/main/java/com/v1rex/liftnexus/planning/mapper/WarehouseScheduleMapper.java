package com.v1rex.liftnexus.planning.mapper;

import com.v1rex.liftnexus.forklift.domain.Forklift;
import com.v1rex.liftnexus.forklift.mapper.ForkliftMapper;
import com.v1rex.liftnexus.planning.dto.WarehouseScheduleResponse;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.mapper.StorageBinMapper;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.mapper.TransportOrderMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WarehouseScheduleMapper {

  private final StorageBinMapper storageBinMapper;
  private final ForkliftMapper forkliftMapper;
  private final TransportOrderMapper transportOrderMapper;

  public WarehouseScheduleResponse toResponse(
      List<StorageBin> storageBins,
      List<Forklift> forklifts,
      List<TransportOrder> unassignedTransportOrders) {

    return new WarehouseScheduleResponse(
        storageBins.stream().map(storageBinMapper::toResponse).toList(),
        forklifts.stream().map(forkliftMapper::toResponse).toList(),
        unassignedTransportOrders.stream().map(transportOrderMapper::toResponse).toList());
  }
}
