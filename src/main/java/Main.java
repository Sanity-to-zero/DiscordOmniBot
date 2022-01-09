import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


    enum Tier {
        ULV(8),
        LV (32),
        MV (128),
        HV (512),
        EV (2048),
        IV (8192),
        LUV(32768),
        ZPM(131072),
        UV (524288),
        MAX(2147483647);

        private final int level;
        Tier(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
public class Main extends ListenerAdapter {
    /*final int ULV = 8;
    final int LV = 32;
    final int MV = 128;
    final int HV = 512;
    final int EV = 2048;
    final int IV = 8192;
    final int LUV = 32768;
    final int ZPM = 131072;
    final int UV = 524288;
    final long MAX = 2147483647;*/

    public static void main(String[] args) throws LoginException {
        try {
            JDA jda = JDABuilder.createDefault("OTE4MzY5MTQzNDQ1NjU1NTcz.YbGP6g.7cbG34c_HpRuYCcDW2bJGOgWpok") // The token of the account that is logging in.
                    .addEventListeners(new Main())   // An instance of a class that will handle events.
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            System.out.println("Finished Building JDA!");
        } catch (LoginException | InterruptedException e) {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        } //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
        // the waiting can be interrupted. This is the exception that would fire in that situation.
        //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
        // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        JSONParser jsonParser = new JSONParser();
        JDA jda = event.getJDA();
        long responseNumber = event.getResponseNumber();//The amount of discord events that JDA has received since the last reconnect.
        Message message = event.getMessage();    // message received
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
        User author = event.getAuthor();  // user that sent message

        String msg = message.getContentDisplay(); // returns a string of text similar to client view

        boolean bot = author.isBot(); //determine if from a bot
        if (event.isFromType(ChannelType.TEXT)) {
            Guild guild = event.getGuild();         //server this was sent in
            TextChannel textChannel = event.getTextChannel(); // Text channel this was sent to
            Member member = event.getMember();     //member that sent the message-- contains server specific info about user

            String name;
            if (message.isWebhookMessage())  {
                name=author.getName();
            }
            else{
                name = member.getEffectiveName();
            }
            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg); // logs server name, channel, user(w/nick), msg
        } else if (event.isFromType(ChannelType.PRIVATE)) {
            PrivateChannel privateChannel = event.getPrivateChannel();

            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }

        if (msg.equals("{ping")) {// ping pong
            channel.sendMessage("pong!").queue();
        } else if (msg.equals("{roll")) { //random number 1-6
            Random rand = ThreadLocalRandom.current();
            int roll = rand.nextInt(6) + 1;
            channel.sendMessage("Your roll: " + roll)
                    .flatMap(
                            (v) -> roll < 3,
                            sentMessage -> channel.sendMessage("The roll for messageId: " + sentMessage.getId() + " wasn't very good...")
                    ).queue();
        } else if (msg.equals("{stop")) { //shutdown
            jda.shutdown();
            jda.shutdownNow();
        } else if (msg.startsWith("{recipe")){
            String processMsg = msg;
            processMsg.trim(); processMsg = processMsg.substring(8); // removes {recipe
            String[] extractTime = processMsg.split("%");
            String[] splitEuTime = extractTime[0].split(":");
            int time = Integer.parseInt(splitEuTime[0]);
            long euTick = Long.parseLong(splitEuTime[1]);
            String machine = splitEuTime[2];
            String[] list = extractTime[1].split(" ", 0);
            System.out.println(extractTime[1]);
            String[][] data = new String[list.length][3];
            byte m = 0;
            for (String chunks:
                 list) {
                data[m] = chunks.split(":");
                ++m;

            }
            for (String[] items :
                    data) {
                for (String chunks :
                        items) {
                    chunks.trim();
                    System.out.println(chunks);
                }
            }

            System.out.println(data[1][1]);
            try (FileReader reader = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\recipes.json")) {
                byte outputCounter = 0;
                for (String[] items :
                        data) {
                    System.out.println(items[1]);
                    if (items[1].equals("O")) {
                        outputCounter++;
                    }
                }
                String[][] outputs = new String[outputCounter][3];
                String[][] ingredients = new String[data.length - outputCounter][3];
                m = 0;
                byte n = 0;
                for (String[] datum : data) { // separate data into inputs and outputs
                    if (datum[1].equals("O")) {
                        outputs[m] = datum;
                        m++;
                    } else {
                        ingredients[n] = datum;
                        n++;
                    }
                }
                Object obj = jsonParser.parse(reader);
                JSONObject recipeList = (JSONObject) obj;
                boolean recipeAlreadyExists = false;
                try {

                    for (String[] items:
                         outputs) {
                        System.out.println(recipeList.get(items[0]));
                        if (recipeList.get(items[0]) != null) {
                            recipeAlreadyExists = true;

                            channel.sendMessage("Recipe already exists: " + items[0]).queue();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!recipeAlreadyExists) {
                    for (String[] items :
                            outputs) {
                        System.out.println(items[0]);
                        recipeList.put(items[0], outputs[0][0] + ".json");
                        System.out.println(recipeList.get(items[0]));
                    }

                    try (FileWriter fileWriter = new FileWriter("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\recipes.json")) {
                        fileWriter.write(recipeList.toJSONString());
                        fileWriter.flush();
                        for (String[] output :
                        outputs){
                            channel.sendMessage("created recipe for " + output[0]).queue();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                     // organize data into json
                    JSONObject outputObj = new JSONObject();
                    JSONArray inputsList = new JSONArray();

                    for (String[] items : // create array of json objects of each ingredient formatted as {name, type, count} per element
                            ingredients) {
                        JSONObject ingredient = new JSONObject();
                        ingredient.put("name", items[0]);
                        ingredient.put("type", items[2]);
                        ingredient.put("count", Integer.parseInt(items[3]));
                        inputsList.add(ingredient);
                    }
                    JSONArray outputsList = new JSONArray();
                    for (String[] items : // same thing but with outputs
                            outputs) {
                        JSONObject product = new JSONObject();
                        product.put("name", items[0]);
                        product.put("type", items[2]);
                        product.put("count", Integer.parseInt(items[3]));
                        outputsList.add(product);
                    }
                    outputObj.put("inputs", inputsList);// place arrays into attributes named inputs and outputs
                    outputObj.put("outputs", outputsList);
                    outputObj.put("time",time);
                    outputObj.put("energy", euTick);
                    outputObj.put("machine", machine);

                    String recipeData = outputObj.toJSONString();
                    try (FileWriter writer = new FileWriter("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\" + outputs[0][0] + ".json")) {
                        writer.write(recipeData);
                    }
                }


            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        } else if (msg.startsWith("{check")) {
            String inputMessage = msg.substring(7).trim();
            String check = checkExists(inputMessage);
            if (check == null) {
                channel.sendMessage("Recipe not created yet!").queue();
            } else {
                channel.sendMessage("```json\n" + check + "\n```").queue();
            }


        } else if (msg.equals("{help")) {
            channel.sendMessage("{ping - pong!\n" +
                    "{roll - rolls between 1-6\n" +
                    "{stop - shuts down bot\n" +
                    "{recipe - creates new recipe\n" +
                    "formatting: {recipe [time in ticks]:[eu/t]:machineName%resource:[I/O]:[item/fluid]:count resource:[I/O]:[item/fluid]:count\n" +
                    "must always have output and input. Will name files after first input\n" +
                    "{check - {check [item] returns the json contents of the recipe or not exists message\n" +
                    "{materials resource:count\n" +
                    "{efficiency - {efficiency resource:[items per tick]:[ULV,LV,MV,...]\n" +
                    "returns a list of the machines you will need to get peak efficiency with tier-cap").queue();
        } else if (msg.startsWith("{materials")) {
            String inputMessage = msg.substring(11);
            String[] bufferThing = inputMessage.trim().split(":");
            String check = checkExists(bufferThing[0]);
            if (check == null) {
                channel.sendMessage("Recipe not created yet!").queue();
            } else {
                String toCraft = bufferThing[0];
                int count = Integer.parseInt(bufferThing[1]);
                ArrayList[] baseMaterialsList = materialsCost(toCraft,count);
                if (baseMaterialsList == null) {
                    channel.sendMessage("an error has occurred?").queue();
                } else {
                    Iterator[] iterators = new Iterator[]{baseMaterialsList[0].iterator(), baseMaterialsList[1].iterator()};
                    while (iterators[0].hasNext()) {
                        channel.sendMessage(iterators[0].next() + " : " + iterators[1].next()).queue();
                    }
                }

            }
        } else if(msg.startsWith("{efficiency")) {
            String inputMessage = msg.substring(12);
            String[] buff = inputMessage.split(":");
            String resource = buff[0];
            float targetSpeed = Float.parseFloat(buff[1]);
            Tier tierCap = findTier(buff[2]);

            String check = checkExists(resource);
            if (check == null) {
                channel.sendMessage("Recipe not created yet!").queue();
            } else {
                List[] machinesList = calculateMachines(resource, targetSpeed, tierCap);
                if (machinesList == null) {
                    channel.sendMessage("an error has occurred?").queue();
                } else {
                    Iterator[] iterators = new Iterator[]{machinesList[0].iterator(), machinesList[1].iterator(),
                            machinesList[2].iterator(), machinesList[3].iterator()};
                    while (iterators[0].hasNext()) {
                        channel.sendMessage(iterators[0].next() +
                                "\n tier:" +iterators[1].next() +
                                "\n for: " + iterators[2].next() +
                                "\n count: " + iterators[3].next());
                    }
                }
            }

        }
    }

    public static String checkExists(String item) {
        try (FileReader file = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\recipes.json")) {
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(file);
            JSONObject json = (JSONObject) obj;
            if (json.get(item) == null) {
                return null;
            } else {
                try (FileReader recipe = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\" + json.get(item))){
                    Object recipeObj = jsonParser.parse(recipe);
                    JSONObject recipeJSON = (JSONObject) recipeObj;
                    return recipeJSON.toJSONString();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "error has occurred";
    }

    public static ArrayList[] materialsCost(String name, int count) {
        try (FileReader reader = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\recipes.json")) {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            JSONObject json = (JSONObject) obj;
            ArrayList[] materialsList = {new ArrayList<String>(), new ArrayList<Integer>()};
            if (json.get(name) != null) {
                try (FileReader reader1 = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\" + json.get(name))){
                    Object recipeOBJ = parser.parse(reader1);
                    JSONObject recipeJSON = (JSONObject) recipeOBJ;
                    JSONArray inputs = (JSONArray) recipeJSON.get("inputs");
                    JSONArray outputs = (JSONArray) recipeJSON.get("outputs");
                    JSONObject[] inputsData = new JSONObject[inputs.size()];
                    JSONObject[] outputsData = new JSONObject[outputs.size()];
                    for (int i = 0; i < inputsData.length; i++) {
                        inputsData[i] = (JSONObject) inputs.get(i);
                    }
                    int crafts = 1;
                    for (int i = 0; i < outputsData.length; i++) {
                        outputsData[i] = (JSONObject) outputs.get(i);
                        if (outputsData[i].get("name").equals(name)) {
                            boolean partial = count % (long) outputsData[i].get("count") > 0;
                            crafts = (int) (count / (long) outputsData[i].get("count"));
                            System.out.println(outputsData[i].get("count"));
                            crafts += partial ? 1 : 0;
                            System.out.printf("%d partial: %b %d",count,partial,crafts);
                        }
                    }

                    for (JSONObject items :
                            inputsData) {
                        boolean base = json.get(items.get("name")) == null;

                        if (base) {
                            materialsList[0].add(items.get("name"));
                            materialsList[1].add((int) ((long)items.get("count") * crafts));
                        } else {
                            Iterator[] iterators = recurseMaterials((String) items.get("name"), (int) ((long) items.get("count") * crafts),json);
                            while (iterators[0].hasNext()) {
                                String item = (String) iterators[0].next();
                                long resourceCount = (long) iterators[1].next();
                                if (materialsList[0].contains(item)) {
                                    for (int i = 0; i < materialsList[0].size(); i++) {
                                        if (materialsList[0].get(i).equals(items.get("name"))) {
                                            int curMaterialCount = (int) materialsList[1].get(i);
                                            materialsList[1].set(i, curMaterialCount + resourceCount);
                                        }
                                    }
                                } else {
                                    materialsList[0].add(item);
                                    materialsList[1].add(resourceCount);
                                }
                            }
                        }
                    }
                    return materialsList;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Iterator[] recurseMaterials(String name, int count, JSONObject recipesJson) {
        ArrayList[] materialsList = {new ArrayList<String>(), new ArrayList<Integer>()};

        try (FileReader reader = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources" + recipesJson.get(name))){
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(reader);
            JSONObject json = (JSONObject) obj;
            JSONArray inputs = (JSONArray) json.get("inputs");
            JSONArray outputs = (JSONArray) json.get("outputs");
            JSONObject[] inputsData = new JSONObject[inputs.size()];
            JSONObject[] outputsData = new JSONObject[outputs.size()];
            for (int i = 0; i < inputsData.length; i++) {
                inputsData[i] = (JSONObject) inputs.get(i);
            }
            int crafts = 1;
            for (int i = 0; i < outputsData.length; i++) {
                outputsData[i] = (JSONObject) outputs.get(i);
                if (outputsData[i].get("name").equals(name)) {
                    boolean partial = count % (long) outputsData[i].get("count") > 0;
                    crafts = (int) (count / (long) outputsData[i].get("count"));
                    System.out.println(outputsData[i].get("count"));
                    crafts += partial ? 1 : 0;
                    System.out.printf("%d partial: %b %d",count,partial,crafts);
                }
            }
            for (JSONObject items :
                    inputsData) {
                boolean base = json.get(items.get("name")) == null;

                if (base) {
                    materialsList[0].add(items.get("name"));
                    materialsList[1].add((long)items.get("count") * crafts);
                } else {
                    Iterator[] iterators = recurseMaterials((String) items.get("name"), (int) ((long) items.get("count") * crafts),recipesJson);
                    while (iterators[0].hasNext()) {
                        String item = (String) iterators[0].next();
                        Integer resourceCount = (int) iterators[1].next();
                        if (materialsList[0].contains(item)) {
                            for (int i = 0; i < materialsList[0].size(); i++) {
                                if (materialsList[0].get(i).equals(items.get("name"))) {
                                    int curMaterialCount = (int) materialsList[1].get(i);
                                    materialsList[1].set(i, curMaterialCount + resourceCount);
                                }
                            }
                        } else {
                            materialsList[0].add(item);
                            materialsList[1].add(resourceCount);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Iterator[]{materialsList[0].iterator(), materialsList[1].iterator()};
    }

    public static ArrayList[] calculateMachines(String name, float targetSpeed, Tier tierCap) {
        JSONParser parser = new JSONParser();
        ArrayList[] machinesList = {
                new ArrayList<String>(), // machine name
                new ArrayList<Tier>(),// tier
                new ArrayList<String>(), // for what
                new ArrayList<Integer>() // count of machines
        };

        try (FileReader reader = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\recipes.json")){
            Object recipesObj = parser.parse(reader);
            JSONObject recipesJson = (JSONObject) recipesObj;
            try (FileReader recipe = new FileReader("C:\\Users\\Pufferunpopped\\IdeaProjects\\DiscordOmniBot\\src\\main\\resources\\" + recipesJson.get(name))){
                Object recipeObj = parser.parse(recipe);
                JSONObject json = (JSONObject) recipeObj;
                int energyPerTickByRecipe = Math.toIntExact((long)json.get("energy"));
                Tier baseTier = findTier(energyPerTickByRecipe);
                float baseSpeed = (float) json.get("time") * (float) energyPerTickByRecipe / (float) baseTier.getLevel();
                int baseMachinesNeeded = (int) Math.ceil(targetSpeed / baseSpeed);

                String machineName = (String) json.get("machine");

                List[] overclockedMachines = overclock(baseMachinesNeeded, tierCap, baseTier);
                Iterator[] iterators = {overclockedMachines[0].iterator(),overclockedMachines[1].iterator()};
                while (iterators[0].hasNext()) {
                    machinesList[0].add(machineName);
                    machinesList[1].add(iterators[1].next());
                    machinesList[2].add(name);
                    machinesList[3].add(iterators[0].next());
                }
                JSONArray inputs = (JSONArray) json.get("inputs");
                JSONObject[] inputsData = new JSONObject[inputs.size()];
                for (JSONObject data :
                        inputsData) {
                    boolean base = recipesJson.get(data.get("name")) == null;
                    if (!base) {
                        //
                        float targetSpeedSub_recipe = targetSpeed * (float) data.get("count");
                        ArrayList[] recurseSubRecipes = calculateMachines((String) data.get("name"),targetSpeedSub_recipe,tierCap);
                        Iterator[] iterators1 = {
                                recurseSubRecipes[0].iterator(),
                                recurseSubRecipes[1].iterator(),
                                recurseSubRecipes[2].iterator(),
                                recurseSubRecipes[3].iterator()
                        };
                        while (iterators[0].hasNext()) {
                            machinesList[0].add(iterators1[0].next());
                            machinesList[1].add(iterators1[1].next());
                            machinesList[2].add(iterators1[2].next());
                            machinesList[3].add(iterators1[3].next());
                        }
                    }
                }
                return machinesList;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    static ArrayList[] overclock(int baseMachinesNeeded, Tier tierCap, Tier baseTier) {
        ArrayList[] machineList = {
                new ArrayList<Integer>(),
                new ArrayList<Tier>()
        };

        int amountCanOverclock = baseMachinesNeeded;
        Tier curTier = baseTier;
        do {
            if (curTier.equals(tierCap)) {
                machineList[0].add(amountCanOverclock);
                machineList[1].add(curTier);
            }
            machineList[0].add(amountCanOverclock % 4); // the stuff that can no longer overclock
            machineList[1].add(curTier);
            amountCanOverclock /= 4;  // integer divide by for so extra stuff is lost
            curTier = findTier(curTier.getLevel() + 1); // increase tier by 1
        } while (amountCanOverclock > 0);
        return machineList;
    }

    static Tier findTier(int energyPerOperation) {
        Tier tier;
        if (energyPerOperation < 8) tier = Tier.ULV;
        else if (energyPerOperation <= 32) tier = Tier.LV;
        else if (energyPerOperation <= 128) tier = Tier.MV;
        else if (energyPerOperation <= 512) tier = Tier.HV;
        else if (energyPerOperation <= 2048) tier = Tier.EV;
        else if (energyPerOperation <= 8192) tier = Tier.IV;
        else if (energyPerOperation <= 32768) tier = Tier.LUV;
        else if (energyPerOperation <= 131072) tier = Tier.ZPM;
        else if (energyPerOperation <= 524288) tier = Tier.UV;
        else tier = Tier.MAX;
        return tier;
    }
    static Tier findTier(String target) {
        Tier tier;
        if      (target.equals("ULV")) tier = Tier.ULV;
        else if (target.equals("LV"))  tier = Tier.LV;
        else if (target.equals("MV"))  tier = Tier.MV;
        else if (target.equals("HV"))  tier = Tier.HV;
        else if (target.equals("EV"))  tier = Tier.EV;
        else if (target.equals("IV"))  tier = Tier.IV;
        else if (target.equals("LUV")) tier = Tier.LUV;
        else if (target.equals("ZPM")) tier = Tier.ZPM;
        else if (target.equals("UV"))  tier = Tier.UV;
        else                           tier = Tier.MAX;
        return tier;
    }

}
