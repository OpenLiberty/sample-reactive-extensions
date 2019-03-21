package io.lilfreelib.server;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.lilfreelib.server.Customer;

import org.apache.cxf.jaxrs.rx2.client.FlowableRxInvoker;
import org.apache.cxf.jaxrs.rx2.client.FlowableRxInvokerProvider;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.Scheduler;

@Path ("/test")
public class ReactiveTestResource {

   private static final int PORT = Integer.getInteger("default.http.port", 9080);
   private static final long CUSTOMER_SLEEP = 3;
   private static final long GET_COLLECTED_DATA_SLEEP = 7;
   private static final long TIME_LOOP = 50000;
   private CountDownLatch countDownLatch = new CountDownLatch(5);

   @GET
   public Response test() {
      Client c = null;
      try {

         StringBuilder result = new StringBuilder("ReactiveTestResource ");
         c = ClientBuilder.newBuilder()
                          .build();

         newCustomer(new Customer("John's", Customer.BEST_SERVICE_LEVEL));
         sleepFor(CUSTOMER_SLEEP);
         newCustomer(new Customer("Jacob's", Customer.BETTER_SERVICE_LEVEL));
         sleepFor(CUSTOMER_SLEEP);
         newCustomer(new Customer("Jingleheimer's", Customer.BEST_SERVICE_LEVEL));
         sleepFor(CUSTOMER_SLEEP);
         newCustomer(new Customer("Schmidt's", Customer.FREE_SERVICE_LEVEL));
         sleepFor(CUSTOMER_SLEEP);
         newCustomer(new Customer("Too's", Customer.BETTER_SERVICE_LEVEL));

         try {
            if (!(countDownLatch.await(3, TimeUnit.MINUTES))) {
               result.append("Test took too long");
            } else {
               result.append("Test was successful");
            }
         } catch (InterruptedException ex) {

         }

         System.out.println(result);
         return Response.ok(result.toString()).build();
      } finally {
         if (c != null) {
            c.close();
         }
      }
   }


   private  void newCustomer(Customer customer) {
      Client c = null;
      try {
         c = ClientBuilder.newBuilder().build();
         Response response = c.target("http://localhost:" + PORT + "/reactive/collecteddatastore/postCustomer")
                              .request()
                              .post(Entity.json(customer));
         System.out.println("newCustomer: " + response.readEntity(String.class));

         Executors.newFixedThreadPool(1).submit(() -> asyncGet(customer));
      } finally {
         if (c != null) {
            c.close();
         }
      }
   }

   private  void asyncGet(Customer customer) {
      Client c = null;
      try {
         c = ClientBuilder.newBuilder()
                          .executorService(Executors.newSingleThreadExecutor(Executors.defaultThreadFactory()))
                          .register(FlowableRxInvokerProvider.class)
                          .build();

         Flowable<List<String>> flowable = c.target("http://localhost:" + PORT + "/reactive/collecteddatastore/rxget/")
                                            .path(customer.getName())
                                            .request()
                                            .rx(FlowableRxInvoker.class).get(new GenericType<List<String>>() {});

         final Holder<List<String>> holder = new Holder<List<String>>();

         long endTime = System.currentTimeMillis() + TIME_LOOP;
         while (System.currentTimeMillis() < endTime) {

            flowable
               .observeOn(Schedulers.computation(), true)
               .subscribe(v -> {
                             holder.value = v; // onNext
                             if (holder.value.size() > 10) {
                                // Shorten the output for the sample
                                System.out.println("asyncGet: " + customer.getName() + " onNext " + holder.value.get(0) + ", " + holder.value.get(1) + " - " + holder.value.get(holder.value.size() - 1));
                             } else {
                                System.out.println("asyncGet: " + customer.getName() + " onNext " + holder.value);
                             }
                          },
                          throwable -> {
                             System.out.println("asyncGet: " + customer.getName() + " onError " + throwable.getMessage()); // onError
                          },
                          () -> System.out.println("asyncGet: " + customer.getName() + " onCompleted ")); // onCompleted
            sleepFor(GET_COLLECTED_DATA_SLEEP);
         }

         List<String> response = holder.value;
         System.out.println("asyncGet2: " + customer.getName() + " " + response.get(response.size() - 1));
         countDownLatch.countDown();
      } finally {
         if (c != null) {
            c.close();
         }
      }
   }

   private static void sleepFor(long seconds) {
      try {
         Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
      } catch (InterruptedException e) {
         //ignore
      }
   }

   private class Holder<T> {
      public volatile T value;
   }

}
