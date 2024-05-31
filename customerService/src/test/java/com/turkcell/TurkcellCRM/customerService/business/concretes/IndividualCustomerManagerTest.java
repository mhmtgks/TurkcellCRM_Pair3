package com.turkcell.TurkcellCRM.customerService.business.concretes;

import com.turkcell.TurkcellCRM.customerService.adapter.MernisService;
import com.turkcell.TurkcellCRM.customerService.business.rules.IndividualCustomerBusinessRules;
import com.turkcell.TurkcellCRM.customerService.clients.TokenControlClient;
import com.turkcell.TurkcellCRM.customerService.core.crossCuttingConcerns.exceptions.types.BusinessException;
import com.turkcell.TurkcellCRM.customerService.core.crossCuttingConcerns.mapping.ModelMapperManager;
import com.turkcell.TurkcellCRM.customerService.core.crossCuttingConcerns.mapping.ModelMapperService;
import com.turkcell.TurkcellCRM.customerService.dataAccess.IndividualCustomerRepository;
import com.turkcell.TurkcellCRM.customerService.dtos.request.create.CreateIndividualCustomerRequest;
import com.turkcell.TurkcellCRM.customerService.dtos.request.update.UpdateIndividualCustomerRequest;
import com.turkcell.TurkcellCRM.customerService.dtos.response.get.GetAllIndividualCustomerResponse;
import com.turkcell.TurkcellCRM.customerService.dtos.response.update.UpdatedIndividualCustomerResponse;
import com.turkcell.TurkcellCRM.customerService.entities.concretes.IndividualCustomer;
import com.turkcell.TurkcellCRM.customerService.entities.enums.Gender;
import com.turkcell.TurkcellCRM.customerService.kafka.producers.IndividualCustomerProducer;
import org.hibernate.mapping.Any;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IndividualCustomerManagerTest {
    private IndividualCustomerManager individualCustomerManager;
    private IndividualCustomerRepository individualCustomerRepository;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private ModelMapper modelMapper;
    private ModelMapperService modelMapperService;
    private MernisService mernisService;
    private TokenControlClient tokenControlClient;
    private IndividualCustomerBusinessRules rules;


    @BeforeEach
    void setUp(){
        individualCustomerRepository = mock(IndividualCustomerRepository.class);
        modelMapper = new ModelMapper();
        modelMapperService=new ModelMapperManager(modelMapper);
        mernisService = mock(MernisService.class);
        tokenControlClient = mock(TokenControlClient.class);
        rules = new IndividualCustomerBusinessRules(individualCustomerRepository, mernisService,tokenControlClient);
        kafkaTemplate = mock(KafkaTemplate.class);
        IndividualCustomerProducer producer = new IndividualCustomerProducer(kafkaTemplate);
        individualCustomerManager = new IndividualCustomerManager(individualCustomerRepository,modelMapperService,rules,producer);

    }

    @Test
    void deleteById(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.of(new IndividualCustomer()));
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);

        individualCustomerManager.delete(1,"Authorization");
        assert true;
    }
    @Test
    void deleteWithUnAuthorizedException(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.of(new IndividualCustomer()));
        when(tokenControlClient.tokenControl(anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.delete(1,"Authorization" );
        });
    }
    @Test
    void deleteWithNotExistsId_ShouldThrowException(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.delete(1,"Authorization" );
        });
    }

    @Test
    void getById(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.of(new IndividualCustomer()));
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);

        individualCustomerManager.getById(1,"Authorization" );
        assert true;
    }
    @Test
    void getByIdWithUnAuthorizedException(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.of(new IndividualCustomer()));
        when(tokenControlClient.tokenControl(anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.getById(1,"Authorization" );
        });
    }

    @Test
    void getByIdWithNotExistsId_ShouldThrowException(){
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.empty());
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.getById(1,"Authorization");
        });

    }

    @Test
    void getAll(){
        IndividualCustomer customer1 = new IndividualCustomer();
        IndividualCustomer customer2 = new IndividualCustomer();
        List<IndividualCustomer> list = new ArrayList<>();

        list.add(customer1);
        list.add(customer2);
        when(individualCustomerRepository.findAll()).thenReturn(list);
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);
        List<GetAllIndividualCustomerResponse> result = individualCustomerManager.getAll("Authorization");

        assertEquals(2, result.size());
    }
    @Test
    void getAllWithUnAuthorizedException(){
        when(tokenControlClient.tokenControl(anyString())).thenReturn(false);
        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.getAll("Authorization" );
        });
    }

    @Test
    void getAllShouldThrowException(){
        when(individualCustomerRepository.findAll()).thenReturn(new ArrayList<IndividualCustomer>());

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.getAll("Authorization");
        });
    }


    //TODO: add için yazılan testlerdeki ortak alanları fonksiyona çevir ve o fonksiyonları çağır.
    @Test
    void add(){
        IndividualCustomer customer = new IndividualCustomer();

        customer.setBirthDate(LocalDateTime.now());
        customer.setNationalityNumber("12345678902");
        customer.setFirstName("Deneme");
        customer.setLastName("Deneme");
        customer.setGender(Gender.MALE);
        customer.setFatherName("Deneme");
        customer.setMotherName("Deneme");

        CreateIndividualCustomerRequest request = new CreateIndividualCustomerRequest();
//
        request= modelMapperService.forRequest().map(customer,CreateIndividualCustomerRequest.class);

        when(individualCustomerRepository.findByNationalityNumber("98765432102")).thenReturn(Optional.empty());

        when(individualCustomerRepository.save(customer)).thenReturn(new IndividualCustomer());


        when(mernisService.checkIsRealPerson(request)).thenReturn(true);
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);
        individualCustomerManager.add(request,"Authorization");
    }
    @Test
    void addWithUnAuthorizedException(){
        when(tokenControlClient.tokenControl(anyString())).thenReturn(false);
        CreateIndividualCustomerRequest request = new CreateIndividualCustomerRequest();
        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.add(request,"Authorization" );
        });
    }

    @Test
    void addShouldThrowExceptionForIndividualCustomerExists(){

        IndividualCustomer customer = new IndividualCustomer();

        customer.setBirthDate(LocalDateTime.now());
        customer.setNationalityNumber("12345678902");
        customer.setFirstName("Deneme");
        customer.setLastName("Deneme");
        customer.setGender(Gender.MALE);
        customer.setFatherName("Deneme");
        customer.setMotherName("Deneme");

        CreateIndividualCustomerRequest request =modelMapperService.forResponse().map(customer, CreateIndividualCustomerRequest.class);
        when(individualCustomerRepository.findByNationalityNumber("12345678902")).thenReturn(Optional.of(new IndividualCustomer()));
        when(individualCustomerRepository.save(customer)).thenReturn(new IndividualCustomer());

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.add(request,"Authorization");
        });

    }

    @Test
    void addShouldThrowExceptionForCheckMernis(){

        IndividualCustomer customer = new IndividualCustomer();

        customer.setBirthDate(LocalDateTime.now());
        customer.setNationalityNumber("12345678902");
        customer.setFirstName("Deneme");
        customer.setLastName("Deneme");
        customer.setGender(Gender.MALE);
        customer.setFatherName("Deneme");
        customer.setMotherName("Deneme");

        CreateIndividualCustomerRequest request =modelMapperService.forRequest().map(customer,CreateIndividualCustomerRequest.class);

        when(individualCustomerRepository.findByNationalityNumber("98765432102")).thenReturn(Optional.empty());
        when(mernisService.checkIsRealPerson(request)).thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            individualCustomerManager.add(request,"Authorization");
        });
    }

    @Test
    void update(){
        IndividualCustomer customer = new IndividualCustomer();

        customer.setBirthDate(LocalDateTime.now());
        customer.setNationalityNumber("12345678902");
        customer.setFirstName("Deneme");
        customer.setLastName("Deneme");
        customer.setGender(Gender.MALE);
        customer.setFatherName("Deneme");
        customer.setMotherName("Deneme");
        customer.setId(1);

        UpdateIndividualCustomerRequest request = new UpdateIndividualCustomerRequest();

        modelMapperService.forRequest().map(request,IndividualCustomer.class);
        CreateIndividualCustomerRequest createIndividualCustomerRequest=modelMapperService.forRequest().map(customer, CreateIndividualCustomerRequest.class);
        when(individualCustomerRepository.findByNationalityNumber("98765432102")).thenReturn(Optional.of(new IndividualCustomer()));
        when(individualCustomerRepository.findById(1)).thenReturn(Optional.of(new IndividualCustomer()));
        when(individualCustomerRepository.save(customer)).thenReturn(new IndividualCustomer());
        when(mernisService.checkIsRealPerson(any())).thenReturn(true);
        when(tokenControlClient.tokenControl(anyString())).thenReturn(true);

        individualCustomerManager.update(request,1,"Autherization");

    }

    }




    // TODO: update için gerekli test fonksiyonlarını yaz
