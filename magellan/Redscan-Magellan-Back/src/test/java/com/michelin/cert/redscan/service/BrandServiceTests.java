package com.michelin.cert.redscan.service;

import com.michelin.cert.redscan.config.ScanConfig;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageException;
import com.michelin.cert.redscan.utils.datalake.DatalakeStorageItem;
import com.michelin.cert.redscan.utils.models.Brand;
import com.michelin.cert.redscan.utils.models.ServiceLevel;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Brand service tests.
 *
 * @author Axel REMACK
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BrandServiceTests {
  @MockBean
  Brand item;

  @InjectMocks
  BrandService brandService;

  @Mock
  ScanConfig scanConfig;

  @Mock
  RabbitTemplate rabbitTemplate;


  @DisplayName("Test for Brands : Find All")
  @Test
  void testFindAll() throws DatalakeStorageException {
    Brand item1 = new Brand("brand1");
    Brand item2 = new Brand("brand2");
    List<DatalakeStorageItem> brands = Arrays.asList(item1, item2);

    Mockito.when(item.findAll()).thenReturn(brands);
    List<Brand> result = brandService.findAll();
    Mockito.verify(item,Mockito.times(1)).findAll();

    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getName()).isEqualTo(item1.getName());
    assertThat(result.get(1).getName()).isEqualTo(item2.getName());
  }

  @DisplayName("Test for Brands : Find All with pagination")
  @Test
  public void testFindAllWithPagination() throws DatalakeStorageException {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");
    List<DatalakeStorageItem> brands_page1 = Arrays.asList(brand1);
    List<DatalakeStorageItem> brands_page2 = Arrays.asList(brand2);

    Mockito.when(item.findAll("1", "1")).thenReturn(brands_page1);
    List<Brand> result = brandService.findAll("1", "1");
    Mockito.verify(item,Mockito.times(1)).findAll("1", "1");
    Mockito.verify(item,Mockito.times(0)).findAll();

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getName()).isEqualTo(brand1.getName());

    Mockito.when(item.findAll("2", "1")).thenReturn(brands_page2);
    result = brandService.findAll("2", "1");
    Mockito.verify(item,Mockito.times(1)).findAll("2", "1");
    Mockito.verify(item,Mockito.times(0)).findAll();

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getName()).isEqualTo(brand2.getName());
  }

  @DisplayName("Test for Brands : Find item")
  @Test
  public void testFindItem() throws DatalakeStorageException {
    Brand brand1 = new Brand("brand1");
    Brand brand2 = new Brand("brand2");

    Mockito.when(item.find(brand1.getId())).thenReturn(brand1);
    Brand result = brandService.find(brand1.getId());
    Mockito.verify(item,Mockito.times(1)).find(brand1.getId());

    assertThat(result.getId()).isEqualTo(brand1.getId());
    assertThat(result.getId()).isNotEqualTo(brand2.getId());
    assertThat(result.getName()).isEqualTo(brand1.getName());
  }

  @DisplayName("Test for Brands : Create item")
  @Test
  public void testCreateItem() throws DatalakeStorageException {
    Mockito.when(item.create()).thenReturn(true);
    Mockito.when(item.upsert()).thenReturn(true);

    boolean created = brandService.create(item);

    Mockito.verify(item,Mockito.times(1)).create();
    Mockito.verify(item,Mockito.times(2)).upsert();

    assertThat(created).isTrue();
  }

  @DisplayName("Test for Brands : Delete item")
  @Test
  public void testDeleteItem() throws DatalakeStorageException {
    Mockito.when(item.find(null)).thenReturn(item);
    Mockito.when(item.delete()).thenReturn(true);

    boolean deleted = brandService.delete(item.getId());

    Mockito.verify(item,Mockito.times(1)).find(null);
    Mockito.verify(item,Mockito.times(1)).delete();

    assertThat(deleted).isTrue();
  }

  @DisplayName("Test for Brands : Update item")
  @Test
  public void testUpdateItem() throws DatalakeStorageException {
    Mockito.when(item.find()).thenReturn(item);
    Mockito.when(item.upsert()).thenReturn(true);

    boolean updated = brandService.update(item);

    Mockito.verify(item,Mockito.times(1)).find();
    Mockito.verify(item,Mockito.times(1)).upsert();

    assertThat(updated).isTrue();
  }

  @DisplayName("Test for Brands : Block item")
  @Test
  public void testBlockItem() throws DatalakeStorageException {
    Mockito.when(item.find(null)).thenReturn(item);
    Mockito.when(item.block()).thenReturn(true);

    boolean blocked = brandService.block(item.getId());

    Mockito.verify(item,Mockito.times(1)).find(null);
    Mockito.verify(item,Mockito.times(1)).block();

    assertThat(blocked).isTrue();
  }

  @DisplayName("Test for Brands : Unblock item")
  @Test
  public void testUnblockItem() throws DatalakeStorageException {
    Mockito.when(item.find(null)).thenReturn(item);
    Mockito.when(item.unblock()).thenReturn(true);

    boolean unblocked = brandService.unblock(item.getId());

    Mockito.verify(item,Mockito.times(1)).find(null);
    Mockito.verify(item,Mockito.times(1)).unblock();

    assertThat(unblocked).isTrue();
  }

  @DisplayName("Test for Brands : Get items by service level")
  @Test
  void testGetItemsByServiceLevel() throws DatalakeStorageException {
    Brand brandGold1 = new Brand("brandGold1");
    Brand brandGold2 = new Brand("brandGold2");
    Brand brandSilver1 = new Brand("brandSilver1");

    List<DatalakeStorageItem> brandsGold = Arrays.asList(brandGold1, brandGold2);
    Mockito.when(item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.GOLD.getValue())))).thenReturn(brandsGold);
    List<Brand> resultGold = brandService.getBrandsByServiceLevel(ServiceLevel.GOLD);

    Mockito.verify(item,Mockito.times(1)).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.GOLD.getValue())));
    assertThat(resultGold.size()).isEqualTo(2);
    assertThat(resultGold.get(0).getName()).isEqualTo(brandGold1.getName());
    assertThat(resultGold.get(1).getName()).isEqualTo(brandGold2.getName());

    List<DatalakeStorageItem> brandsSilver = Arrays.asList(brandSilver1);
    Mockito.when(item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.SILVER.getValue())))).thenReturn(brandsSilver);
    List<Brand> resultSilver = brandService.getBrandsByServiceLevel(ServiceLevel.SILVER);

    Mockito.verify(item,Mockito.times(1)).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.SILVER.getValue())));
    assertThat(resultSilver.size()).isEqualTo(1);
    assertThat(resultSilver.get(0).getName()).isEqualTo(brandSilver1.getName());

    List<DatalakeStorageItem> brandsBronze = Arrays.asList();
    Mockito.when(item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.BRONZE.getValue())))).thenReturn(brandsBronze);
    List<Brand> resultBronze = brandService.getBrandsByServiceLevel(ServiceLevel.BRONZE);

    Mockito.verify(item,Mockito.times(1)).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.BRONZE.getValue())));
    assertThat(resultBronze.size()).isEqualTo(0);
  }

  @DisplayName("Test for Brands : Ventilate items")
  @Test
  public void testVentilate() throws DatalakeStorageException {
    ReflectionTestUtils.setField(brandService, "scanConfig", scanConfig);

    Brand brandGold1 = new Brand("brandGold1");
    Brand brandGold2 = new Brand("brandGold2");
    List<DatalakeStorageItem> brandsGold = Arrays.asList(brandGold1, brandGold2);

    assertThat(brandGold1.getLastScanDate()).isNull();
    assertThat(brandGold2.getLastScanDate()).isNull();

    Calendar calendar = Calendar.getInstance();

    int goldPeriod = 1 * 24 * 60;
    int goldInterval = goldPeriod / 2;  // 1 day in minutes

    Mockito.when(item.search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.GOLD.getValue())))).thenReturn(brandsGold);
    Mockito.when(brandService.getServiceLevelPeriod(ServiceLevel.GOLD)).thenReturn(1);

    brandService.ventilate();

    Mockito.verify(item,Mockito.times(1)).search(new JSONObject(String.format("{ \"bool\" : { \"must\" : [{ \"term\" : { \"serviceLevel\" :\"%d\" }}] }}", ServiceLevel.GOLD.getValue())));

    calendar.add(Calendar.MINUTE, goldInterval * -1);
    assertThat(brandGold1.getLastScanDate()).isNotNull();
    assertThat(brandGold1.getLastScanDate()).isEqualToIgnoringMillis(calendar.getTime());

    calendar.add(Calendar.MINUTE, goldInterval * -1);
    assertThat(brandGold2.getLastScanDate()).isNotNull();
    assertThat(brandGold2.getLastScanDate()).isEqualToIgnoringMillis(calendar.getTime());
  }


  @DisplayName("Test for Brands : Reinject items to scan")
  @Test
  public void testReinjectAll() throws DatalakeStorageException {
    ReflectionTestUtils.setField(brandService, "scanConfig", scanConfig);
    ReflectionTestUtils.setField(brandService, "rabbitTemplate", rabbitTemplate);

    Calendar calendar = Calendar.getInstance();

    Brand brand1 = new Brand("brand1");
    brand1.setServiceLevel(ServiceLevel.GOLD.getValue());
    brand1.setLastScanDate(calendar.getTime());   // Recent last scan date
    Brand brand2 = new Brand("brand2");
    brand2.setServiceLevel(ServiceLevel.GOLD.getValue());
    calendar.add(Calendar.DAY_OF_WEEK, -14);
    brand2.setLastScanDate(calendar.getTime());   // Old last scan date
    List<DatalakeStorageItem> brands = Arrays.asList(brand1, brand2);

    Mockito.when(item.findAll()).thenReturn(brands);
    Mockito.when(item.find(brand1.getId())).thenReturn(brand1);
    Mockito.when(item.find(brand2.getId())).thenReturn(brand2);
    Mockito.when(brandService.getServiceLevelPeriod(ServiceLevel.GOLD)).thenReturn(1);

    brandService.reinjectAll();

    Mockito.verify(item,Mockito.times(1)).findAll();
    Mockito.verify(item,Mockito.times(0)).find(brand1.getId());
    Mockito.verify(item,Mockito.times(1)).find(brand2.getId());
    Mockito.verify(rabbitTemplate,Mockito.times(0)).convertAndSend(brand1.getFanoutExchangeName(), "", brand1.toJson()); // Brand1 is not reinjected
    Mockito.verify(rabbitTemplate,Mockito.times(1)).convertAndSend(brand2.getFanoutExchangeName(), "", brand2.toJson()); // Brand2 is reinjected
  }

  @DisplayName("Test for Brands : Manually reinject an item")
  @Test
  public void testReinjectItem() throws DatalakeStorageException {
    ReflectionTestUtils.setField(brandService, "rabbitTemplate", rabbitTemplate);
    Brand brand1 = new Brand("brand1");
    Mockito.when(item.find(brand1.getId())).thenReturn(brand1);

    boolean reinjected = brandService.reinject(brand1.getId());

    Mockito.verify(item,Mockito.times(1)).find(brand1.getId());
    Mockito.verify(rabbitTemplate,Mockito.times(1)).convertAndSend(brand1.getFanoutExchangeName(), "", brand1.toJson());

    assertThat(reinjected).isTrue();
  }


}
