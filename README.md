# Json-Database-test-project
Json Database test project in java, was created to practice sockets, gson library, jcommander

##Instructions

- First of all we need to start the server (.\src\main\java\server\Main.class)

- Secondly, we run the client (.\src\main\java\client\Main.class) specifying the action we want to take in the program arguments.
      Values that can be specified in the program arguments: 
        - **-t** - type, takes values: "set", "get" , "delete", "exit" (e.g. "-t set" corresponds to "type":"set")
        - **-k** - key, e.g. "key":"1" corresponds to "-k 1"
        - **-v** - value, e.g. " -v "Hello world!" " corresponds to "value":"Hello world!"
        - **-in** - if you want to use your own json document you need to put it in the ".\src\main\java\client\data\" folder and specify the name of the file.
                    E.g. -in getFile.json. The format of the document must be as follows: 
                    
                        *{
                             "type":"",
                             "key":"",
                             "value":{

                              }
                         }*
                    
                    It is also possible to specify nested keys (e.g. {"type":"get","key":["person","name"]})
