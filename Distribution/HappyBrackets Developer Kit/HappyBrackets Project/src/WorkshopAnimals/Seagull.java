package WorkshopAnimals;

public class Seagull extends Bird {
    @Override
    Animal[] BearYoung() {
        return bearYoungFromEggs();
    }

    @Override
    public Animal[] bearYoungFromEggs() {
        return new Seagull[1];
    }
}
