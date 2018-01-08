package WorkshopAnimals;

public class Seagull extends Bird {
    @Override
    public Bird[] bearYoungFromEggs() {
        return new Seagull[1];
    }
}
