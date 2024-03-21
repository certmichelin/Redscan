package com.michelin.cert.redscan.api;

import com.michelin.cert.redscan.api.exception.ConflictException;
import com.michelin.cert.redscan.api.exception.NotFoundException;
import com.michelin.cert.redscan.service.BrandService;
import com.michelin.cert.redscan.utils.models.Brand;

import kong.unirest.json.JSONObject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Brand controller tests.
 *
 * @author Axel REMACK
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BrandControllerTests {
  @MockBean
  BrandService brandService;

  @InjectMocks
  BrandController brandController;

  @DisplayName("Test for Brands : Find All")
  @Test
  public void testFindAll() {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");
    List<Brand> brands = Arrays.asList(brand1, brand2);

    Mockito.when(brandService.findAll()).thenReturn(brands);
    List<Brand> result = brandController.findAll();
    Mockito.verify(brandService,Mockito.times(1)).findAll();

    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getName()).isEqualTo(brand1.getName());
    assertThat(result.get(1).getName()).isEqualTo(brand2.getName());
  }

  @DisplayName("Test for Brands : Find All with pagination")
  @Test
  public void testFindAllWithPagination() {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");
    List<Brand> brands_page1 = Arrays.asList(brand1);
    List<Brand> brands_page2 = Arrays.asList(brand2);

    Mockito.when(brandService.findAll("1", "1")).thenReturn(brands_page1);
    List<Brand> result = brandController.findAll(1, 1);
    Mockito.verify(brandService,Mockito.times(1)).findAll("1", "1");
    Mockito.verify(brandService,Mockito.times(0)).findAll();

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getName()).isEqualTo(brand1.getName());

    Mockito.when(brandService.findAll("2", "1")).thenReturn(brands_page2);
    result = brandController.findAll(2, 1);
    Mockito.verify(brandService,Mockito.times(1)).findAll("2", "1");
    Mockito.verify(brandService,Mockito.times(0)).findAll();

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getName()).isEqualTo(brand2.getName());
  }

  @DisplayName("Test for Brands : Find item")
  @Test
  public void testFindItem() {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");

    Mockito.when(brandService.find(brand1.getId())).thenReturn(brand1);
    Brand result = brandController.find(brand1.getId());
    Mockito.verify(brandService,Mockito.times(1)).find(brand1.getId());

    assertThat(result.getId()).isEqualTo(brand1.getId());
    assertThat(result.getId()).isNotEqualTo(brand2.getId());
    assertThat(result.getName()).isEqualTo(brand1.getName());
  }

  @DisplayName("Test for Brands : Find item that does not exists (throws NotFoundException)")
  @Test
  public void testFindNonExistentItem() {
    Mockito.when(brandService.find("not_existing_brand")).thenReturn(null);

    assertThrows(NotFoundException.class, () -> brandController.find("not_existing_brand"), "NotFoundException was expected");

    Mockito.verify(brandService,Mockito.times(1)).find("not_existing_brand");
  }

  @DisplayName("Test for Brands : Create item")
  @Test
  public void testCreateItem() {
    Mockito.when(brandService.create(Mockito.any(Brand.class))).thenReturn(true);
    boolean created = brandController.create(new Brand("brandTest"));
    Mockito.verify(brandService,Mockito.times(1)).create(Mockito.any(Brand.class));

    assertThat(created).isTrue();
  }

  @DisplayName("Test for Brands : Create item that was already created (throws ConflictException)")
  @Test
  public void testCreateAlreadyCreatedItem() {
    Mockito.when(brandService.create(Mockito.any(Brand.class))).thenReturn(false);

    assertThrows(ConflictException.class, () -> brandController.create(new Brand("brandTest")), "ConflictException was expected");

    Mockito.verify(brandService,Mockito.times(1)).create(Mockito.any(Brand.class));
  }

  @DisplayName("Test for Brands : Delete item")
  @Test
  public void testDeleteItem() {
    Brand brand = new Brand("brandTest");

    Mockito.when(brandService.delete(brand.getId())).thenReturn(true);
    boolean deleted = brandController.delete(brand.getId());
    Mockito.verify(brandService,Mockito.times(1)).delete(brand.getId());

    assertThat(deleted).isTrue();
  }

  @DisplayName("Test for Brands : Delete item that does not exists (throws NotFoundException)")
  @Test
  public void testDeleteNonExistentItem() {
    Brand brand = new Brand("brandTest");
    Mockito.when(brandService.delete(brand.getId())).thenReturn(false);

    assertThrows(NotFoundException.class, () -> brandController.delete(brand.getId()), "NotFoundException was expected");

    Mockito.verify(brandService,Mockito.times(1)).delete(brand.getId());
  }

  @DisplayName("Test for Brands : Update item")
  @Test
  public void testUpdateItem() {
    Brand brand = new Brand("brandTest");

    Mockito.when(brandService.update(brand)).thenReturn(true);
    boolean updated = brandController.update(brand);
    Mockito.verify(brandService,Mockito.times(1)).update(brand);

    assertThat(updated).isTrue();
  }

  @DisplayName("Test for Brands : Update item with conflicting data (throws ConflictException)")
  @Test
  public void testUpdateItemConflicting() {
    Brand brand = new Brand("brandTest");
    Mockito.when(brandService.update(brand)).thenReturn(false);

    assertThrows(ConflictException.class, () -> brandController.update(brand), "ConflictException was expected");

    Mockito.verify(brandService,Mockito.times(1)).update(brand);
  }

  @DisplayName("Test for Brands : Block item")
  @Test
  public void testBlockItem() {
    Brand brand = new Brand("brandTest");

    Mockito.when(brandService.block(brand.getId())).thenReturn(true);
    boolean blocked = brandController.block(brand.getId());
    Mockito.verify(brandService,Mockito.times(1)).block(brand.getId());

    assertThat(blocked).isTrue();
  }

  @DisplayName("Test for Brands : Block item that is already blocked (throws ConflictException)")
  @Test
  public void testBlockAlreadyBlockedItem() {
    Brand brand = new Brand("brandTest");
    Mockito.when(brandService.block(brand.getId())).thenReturn(false);

    assertThrows(ConflictException.class, () -> brandController.block(brand.getId()), "ConflictException was expected");

    Mockito.verify(brandService,Mockito.times(1)).block(brand.getId());
  }

  @DisplayName("Test for Brands : Unblock item")
  @Test
  public void testUnblockItem() {
    Brand brand = new Brand("brandTest");

    Mockito.when(brandService.unblock(brand.getId())).thenReturn(true);
    boolean unblocked = brandController.unblock(brand.getId());
    Mockito.verify(brandService,Mockito.times(1)).unblock(brand.getId());

    assertThat(unblocked).isTrue();
  }

  @DisplayName("Test for Brands : Unblock item that is not blocked (throws ConflictException)")
  @Test
  public void testUnblockNotBlockedItem() {
    Brand brand = new Brand("brandTest");
    Mockito.when(brandService.unblock(brand.getId())).thenReturn(false);

    assertThrows(ConflictException.class, () -> brandController.unblock(brand.getId()), "ConflictException was expected");

    Mockito.verify(brandService,Mockito.times(1)).unblock(brand.getId());
  }

  @DisplayName("Test for Brands : Find All blocked")
  @Test
  public void testFindAllBlocked() {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");
    List<Brand> brands = Arrays.asList(brand1, brand2);

    Mockito.when(brandService.findAllBlocked()).thenReturn(brands);
    List<Brand> result = brandController.findAllBlocked();
    Mockito.verify(brandService,Mockito.times(1)).findAllBlocked();

    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getName()).isEqualTo(brand1.getName());
    assertThat(result.get(1).getName()).isEqualTo(brand2.getName());
  }

  @DisplayName("Test for Brands : Ventilate items")
  @Test
  public void testVentilate() {
    Mockito.when(brandService.ventilate()).thenReturn(true);
    boolean ventilated = brandController.ventilate();
    Mockito.verify(brandService,Mockito.times(1)).ventilate();

    assertThat(ventilated).isTrue();
  }

  @DisplayName("Test for Brands : Export items")
  @Test
  public void testExport() {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");
    List<Brand> brands = Arrays.asList(brand1, brand2);

    Mockito.when(brandService.findAll()).thenReturn(brands);
    Mockito.when(brandService.getDatalakeStorageItemClass()).thenReturn((Class<Brand>) new Brand().getClass());
    HttpEntity<List<Brand>> export_response = brandController.export();
    Mockito.verify(brandService,Mockito.times(1)).findAll();

    assertThat(export_response.getHeaders()).containsEntry("Content-Disposition", Arrays.asList("attachment; filename=brands.json"));
    assertThat(export_response.getHeaders()).containsEntry("Content-Type", Arrays.asList("application/json"));
    assertThat(export_response.getBody().size()).isEqualTo(2);
    assertThat(export_response.getBody().get(0).getName()).isEqualTo(brand1.getName());
    assertThat(export_response.getBody().get(1).getName()).isEqualTo(brand2.getName());
  }

  @DisplayName("Test for Brands : Import items")
  @Test
  public void testImportItems() {
    Brand existing_brand1 = new Brand("existing_brand1");
    Brand existing_brand2 = new Brand("existing_brand2");
    Brand new_brand1 = new Brand("new_brand1");
    Brand new_brand2 = new Brand("new_brand2");
    ArrayList<Brand> brands = new ArrayList<Brand>(Arrays.asList(existing_brand1, existing_brand2, new_brand1, new_brand2));

    Mockito.when(brandService.find(existing_brand1.getId())).thenReturn(existing_brand1);
    Mockito.when(brandService.find(existing_brand2.getId())).thenReturn(existing_brand2);
    Mockito.when(brandService.find(new_brand1.getId())).thenReturn(null);
    Mockito.when(brandService.find(new_brand2.getId())).thenReturn(null);
    Mockito.when(brandService.create(Mockito.any(Brand.class))).thenReturn(true);
    Mockito.when(brandService.update(Mockito.any(Brand.class))).thenReturn(true);

    int imported = brandController.upload(brands);
    Mockito.verify(brandService,Mockito.times(2)).create(Mockito.any(Brand.class));
    Mockito.verify(brandService,Mockito.times(2)).update(Mockito.any(Brand.class));

    assertThat(imported).isEqualTo(4);
  }

  @DisplayName("Test for Brands : Manually reinject an item")
  @Test
  public void testManuallyReinjectItem() {
    Brand brand = new Brand("brandTest");

    Mockito.when(brandService.reinject(brand.getId())).thenReturn(true);
    boolean reinjected = brandController.reinject(brand.getId());
    Mockito.verify(brandService,Mockito.times(1)).reinject(brand.getId());

    assertThat(reinjected).isTrue();
  }

  @DisplayName("Test for Brands : Manually reinject an item that does not exist (throws NotFoundException)")
  @Test
  public void testManuallyReinjectNonExistentItem() {
    Brand brand = new Brand("brandTest");
    Mockito.when(brandService.reinject(brand.getId())).thenReturn(false);

    assertThrows(NotFoundException.class, () -> brandController.reinject(brand.getId()), "NotFoundException was expected");

    Mockito.verify(brandService,Mockito.times(1)).reinject(brand.getId());
  }

}
