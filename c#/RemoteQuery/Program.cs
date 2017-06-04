using Infinispan.HotRod.Config;
using Infinispan.HotRod;
using System;
using System.IO;
using System.Collections.Generic;
using Quickstart;
using Org.Infinispan.Query.Remote.Client;

namespace Infinispan.Tutorial
{
    public class Simple
    {
        const String ERRORS_KEY_SUFFIX = ".errors";
        const String PROTOBUF_METADATA_CACHE_NAME = "___protobuf_metadata";
        const String NAMED_CACHE = "InMemoryNonSharedIndex";
        static String APP_MENU = "\nAvailable actions:\n" +
        "0. Display available actions\n" +
        "1. Add person\n" +
        "2. Remove person\n" +
        "3. Add phone to person\n" +
        "4. Remove phone from person\n" +
        "5. Query persons by name\n" +
        "6. Query persons by phone\n" +
        "7. Add memo to person\n" +
        "8. Full text search on memo\n" +
        "9. Query for projection (name)\n" +
        "10. Count by departement\n" +
        "11. Display all cache entries\n" +
        "12. Clear cache\n" +
        "13. Quit\n";

        /*
        public void BeforeClass()
        {
            IRemoteCache<int, User> userCache = remoteManager.GetCache<int, User>(NAMED_CACHE);
            userCache.Clear();
            PutUsers(userCache);
            IRemoteCache<int, Account> accountCache = remoteManager.GetCache<int, Account>(NAMED_CACHE);
            PutAccounts(accountCache);
            IRemoteCache<int, Transaction> transactionCache = remoteManager.GetCache<int, Transaction>(NAMED_CACHE);
            PutTransactions(transactionCache);
            */
        static void Main()
        {
            // Create a configuration for a locally-running server
            ConfigurationBuilder conf = new ConfigurationBuilder();
            conf.AddServer().Host("127.0.0.1").Port(11222);
            conf.ConnectionTimeout(90000).SocketTimeout(6000);
            conf.Marshaller(new BasicTypesProtoStreamMarshaller());
            RemoteCacheManager remoteManager = new RemoteCacheManager(conf.Build(), true);

            IRemoteCache<String, String> metadataCache = remoteManager.GetCache<String, String>(PROTOBUF_METADATA_CACHE_NAME);
            metadataCache.Put("quickstart/addressbook.proto", File.ReadAllText("addressbook.proto"));
            if (metadataCache.ContainsKey(ERRORS_KEY_SUFFIX))
            {
                Console.WriteLine("fail: error in registering .proto model");
                Environment.Exit(-1);
            }
            IRemoteCache<int, Person> cache = remoteManager.GetCache<int, Person>(NAMED_CACHE);

            bool quit = false;
            displayActions();
            while (!quit)
            {
                int action = readAction();

                switch (action)
                {
                    case 1:
                        putPerson(cache);
                        break;
                    case 2:
                        removePerson(cache);
                        break;
                    case 3:
                        addPhone(cache);
                        break;
                    case 4:
                        removePhone(cache);
                        break;
                    case 5:
                        queryPersonByName(cache);
                        break;
                    case 6:
                        queryPersonByPhone(cache);
                        break;
                    case 7:
                        addMemo(cache);
                        break;
                    case 8:
                        fullTextOnMemo(cache);
                        break;
                    case 9:
                        projectAllNames(cache);
                        break;
                    case 10:
                         countByDepartment(cache);
                        break;
                    case 11:
                        printAll(cache);
                        break;
                    case 12:
                        cache.Clear();
                        Console.WriteLine("Cache cleared");
                        break;
                    case 13:
                        quit = true;
                        Console.WriteLine("Bye!");
                        break;
                    case 0:
                        displayActions();
                        break;
                    default:
                        Console.Error.WriteLine("Invalid action: " + action);
                        break;
                }
            }
            remoteManager.Stop();
        }

        private static void displayActions()
        {
            Console.WriteLine(APP_MENU);
        }

        private static int readInt(String s)
        {
            while (true)
            {
                try
                {
                    Console.Write(s);
                    return Convert.ToInt32(Console.ReadLine());
                }
                catch
                { }
            }
        }

        private static String readString(String s)
        {
            Console.Write(s);
            return Console.ReadLine();
        }

        private static int readAction()
        {
            return readInt("> ");
        }

        private static void putPerson(IRemoteCache<int, Person> cache)
        {
            Person p = new Person();
            p.Id = readInt("Enter person id: ");
            p.Name = readString("Enter person name: ");
            p.Email = readString("Enter person email: ");
            p.Department = (p.Id % 2 == 0) ? "EVEN" : "ODD";
            cache.Put(p.Id, p);
        }
        private static void removePerson(IRemoteCache<int, Person> cache)
        {
            int id = readInt("Enter person id: ");
            cache.Remove(id);
        }

        private static void addPhone(IRemoteCache<int, Person> cache)
        {
            int id = readInt("Enter person id: ");
            Person p = cache.Get(id);
            if (p == null)
            {
                Console.Error.WriteLine("Person not found");
                return;
            }
            Console.WriteLine(p.ToString());
            var pn = new Person.Types.PhoneNumber();
            pn.Number = readString("Enter phone number: ");
            try
            {
                pn.Type = (Person.Types.PhoneType)Enum.Parse(typeof(Person.Types.PhoneType), readString("Enter phone type: "));
                p.Phone.Add(pn);
                cache.Put(id, p);
            }
            catch
            { }
        }

        private static void removePhone(IRemoteCache<int, Person> cache)
        {
            int id = readInt("Enter person id: ");
            Person p = cache.Get(id);
            if (p == null)
            {
                Console.Error.WriteLine("Person not found");
                return;
            }

            Console.WriteLine(p.ToString());

            int phone_index = readInt("Enter phone index: ");
            if ((phone_index < 0) || (phone_index >= p.Phone.Count))
            {
                Console.Error.WriteLine("Phone index '" + phone_index + "' is out of range.");
                return;
            }

            p.Phone.RemoveAt(phone_index);
            cache.Put(id, p);
        }

        static void queryPersonByPhone(IRemoteCache<int, Person> cache)
        {
            string pn = readString("Enter phone number: ");
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "from quickstart.Person as p where p.phone.number= '" + pn + "'";
            QueryResponse result = cache.Query(qr);
            List<Person> listOfPersons = RemoteQueryUtils.unwrapResults<Person>(result);
            Console.WriteLine("Result set is:");
            foreach (Person p in listOfPersons)
            {
                Console.WriteLine(p.ToString());
            }

        }

        static void queryPersonByName(IRemoteCache<int, Person> cache)
        {
            string name = readString("Enter phone name: ");
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "from quickstart.Person as p where p.name= '" + name + "'";
            QueryResponse result = cache.Query(qr);
            List<Person> listOfPersons = RemoteQueryUtils.unwrapResults<Person>(result);
            Console.WriteLine("Result set is:");
            foreach (Person p in listOfPersons)
            {
                Console.WriteLine(p.ToString());
            }
        }

        static void addMemo(IRemoteCache<int, Person> cache)
        {
            int id = readInt("Enter person id: ");
            Person p = cache.Get(id);
            if (p == null)
            {
                Console.Error.WriteLine("Person not found");
                return;
            }

            Console.WriteLine(p.ToString());

            String text = readString("Enter memo text: ");
            var m = new Person.Types.Memo();
            m.Text = text;
            p.Memo.Add(m);
            cache.Put(id, p);
        }
        //        qr.set_querystring("from quickstart.Person as p where p.memo.text : 'language'");

        static void fullTextOnMemo(IRemoteCache<int, Person> cache)
        {
            string text = readString("Text to find: ");
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "from quickstart.Person as p where p.memo.text : '"+text.Replace("'","_")+"'";
            QueryResponse result = cache.Query(qr);
            List<Person> listOfPersons = RemoteQueryUtils.unwrapResults<Person>(result);
            Console.WriteLine("Result set is:");
            foreach (Person p in listOfPersons)
            {
                Console.WriteLine(p.ToString());
            }

        }

        static void projectAllNames(IRemoteCache<int, Person> cache)
        {
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "select name from quickstart.Person";
            QueryResponse result = cache.Query(qr);
            List<Object[]> listOfNames = RemoteQueryUtils.unwrapWithProjection(result);
            Console.WriteLine("Result set is:");
            foreach (Object[] p in listOfNames)
            {
                Console.WriteLine(p[0].ToString());
            }
        }

        static void countByDepartment(IRemoteCache<int, Person> cache)
        {
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "select p.department, count(p.id) from quickstart.Person p group by p.department";
            QueryResponse result = cache.Query(qr);
            List<Object[]> results = RemoteQueryUtils.unwrapWithProjection(result);
            Console.WriteLine("Result set is:");
            foreach (Object[] p in results)
            {
                Console.WriteLine("Department: " + p[0]+ " count is "+p[1]);
            }
        }

        static void printAll(IRemoteCache<int, Person> cache)
        {
            QueryRequest qr = new QueryRequest();
            qr.QueryString = "from quickstart.Person";
            QueryResponse result = cache.Query(qr);
            List<Person> listOfPersons = RemoteQueryUtils.unwrapResults<Person>(result);
            Console.WriteLine("Result set is:");
            foreach (Person p in listOfPersons)
            {
                Console.WriteLine(p.ToString());
            }
        }
    }
}