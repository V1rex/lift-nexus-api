package com.v1rex.liftnexus.transportorder.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.v1rex.liftnexus.config.TestContainersConfiguration;
import com.v1rex.liftnexus.loadunit.domain.LoadUnit;
import com.v1rex.liftnexus.loadunit.domain.LoadUnitStatus;
import com.v1rex.liftnexus.storagebin.domain.Coordinate3D;
import com.v1rex.liftnexus.storagebin.domain.StorageBin;
import com.v1rex.liftnexus.storagebin.domain.ZoneType;
import com.v1rex.liftnexus.transportorder.domain.TransportOrder;
import com.v1rex.liftnexus.transportorder.domain.TransportOrderStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("TransportOrderRepository Integration Tests")
class TransportOrderRepositoryTest {

  @Autowired private TransportOrderRepository transportOrderRepository;
  @Autowired private TestEntityManager entityManager;

  private StorageBin defaultSourceBin;
  private StorageBin defaultTargetBin;

  @BeforeEach
  void setup() {
    Coordinate3D sourceCoordinate = new Coordinate3D(1, 1, 1);
    Coordinate3D targetCoordinate = new Coordinate3D(10, 10, 1);

    defaultSourceBin =
        entityManager.persistAndFlush(
            StorageBin.builder()
                .binCode("SRC-BIN")
                .coordinate(sourceCoordinate)
                .maxWeightCapacityKg(5000)
                .zoneType(ZoneType.STAGING_IN)
                .build());

    defaultTargetBin =
        entityManager.persistAndFlush(
            StorageBin.builder()
                .binCode("TGT-BIN")
                .coordinate(targetCoordinate)
                .maxWeightCapacityKg(5000)
                .zoneType(ZoneType.STORAGE)
                .build());
  }

  @Nested
  @DisplayName("Tests - Validation Constraints")
  class ValidationConstraints {

    @Test
    @DisplayName("Should fail when target LoadUnit is missing")
    void shouldFailWhenTargetLoadUnitIsNull() {
      TransportOrder invalidOrder =
          TransportOrder.builder()
              .targetLoadUnit(null) // Defect
              .sourceBin(defaultSourceBin)
              .targetBin(defaultTargetBin)
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(invalidOrder))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("targetLoadUnit");
    }

    @Test
    @DisplayName("Should fail when physical source bin is missing")
    void shouldFailWhenSourceBinIsNull() {
      LoadUnit validUnit =
          entityManager.persist(
              LoadUnit.builder()
                  .trackingCode("LU-VAL-1")
                  .weightKg(500)
                  .status(LoadUnitStatus.STORED)
                  .build());

      TransportOrder invalidOrder =
          TransportOrder.builder()
              .targetLoadUnit(validUnit)
              .sourceBin(null) // Defect
              .targetBin(defaultTargetBin)
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(invalidOrder))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("sourceBin");
    }

    @Test
    @DisplayName("Should fail when physical destination bin is missing")
    void shouldFailWhenTargetBinIsNull() {
      LoadUnit validUnit =
          entityManager.persist(
              LoadUnit.builder()
                  .trackingCode("LU-VAL-2")
                  .weightKg(500)
                  .status(LoadUnitStatus.STORED)
                  .build());

      TransportOrder invalidOrder =
          TransportOrder.builder()
              .targetLoadUnit(validUnit)
              .sourceBin(defaultSourceBin)
              .targetBin(null) // Defect
              .build();

      assertThatThrownBy(() -> entityManager.persistAndFlush(invalidOrder))
          .isInstanceOf(ConstraintViolationException.class)
          .hasMessageContaining("targetBin");
    }
  }

  @Nested
  @DisplayName("Tests - searchOrders Custom Query")
  class SearchOrders {
    @Test
    @DisplayName(
        "Should return paginated transportOrders "
            + "filtered by minWeight based on linked LoadUnit")
    void shouldFilterByJoinedLoadUnitWeight() {

      LoadUnit lightUnit =
          entityManager.persist(
              LoadUnit.builder()
                  .trackingCode("LU-500")
                  .weightKg(500)
                  .status(LoadUnitStatus.STORED)
                  .build());

      LoadUnit heavyUnit =
          entityManager.persist(
              LoadUnit.builder()
                  .trackingCode("LU-1500")
                  .weightKg(1500)
                  .status(LoadUnitStatus.STORED)
                  .build());

      transportOrderRepository.save(
          TransportOrder.builder()
              .status(TransportOrderStatus.OPEN)
              .targetLoadUnit(lightUnit)
              .sourceBin(defaultSourceBin)
              .targetBin(defaultTargetBin)
              .build());

      transportOrderRepository.save(
          TransportOrder.builder()
              .status(TransportOrderStatus.COMPLETED)
              .targetLoadUnit(heavyUnit)
              .sourceBin(defaultSourceBin)
              .targetBin(defaultTargetBin)
              .build());

      transportOrderRepository.flush();

      Pageable pageable = PageRequest.of(0, 10);
      Page<TransportOrder> result = transportOrderRepository.searchOrders(null, 1000, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent().get(0).getTargetLoadUnit().getTrackingCode())
          .isEqualTo("LU-1500");
    }

    @Test
    @DisplayName(
        "Should return all transportOrders within "
            + "page limits when all query criteria parameters are null")
    void shouldReturnAllTasks_WhenAllParametersAreNull() {
      LoadUnit unit =
          entityManager.persist(
              LoadUnit.builder()
                  .trackingCode("LU-1")
                  .weightKg(500)
                  .status(LoadUnitStatus.STORED)
                  .build());

      transportOrderRepository.save(
          TransportOrder.builder()
              .status(TransportOrderStatus.OPEN)
              .targetLoadUnit(unit)
              .sourceBin(defaultSourceBin)
              .targetBin(defaultTargetBin)
              .build());

      transportOrderRepository.flush();

      Pageable pageable = PageRequest.of(0, 10);
      Page<TransportOrder> result = transportOrderRepository.searchOrders(null, null, pageable);

      assertThat(result.getTotalElements()).isEqualTo(1);
    }
  }
}
