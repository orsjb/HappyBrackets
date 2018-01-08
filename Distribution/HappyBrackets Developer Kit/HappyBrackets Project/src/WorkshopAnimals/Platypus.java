package WorkshopAnimals;

public class Platypus extends Mammal implements LayEggs {
    @Override
    Animal[] BearYoung() {
        return bearYoungFromEggs();
    }

    @Override
    void nurseBabiesWithMilk() {
        // BabiesDrinkMilkFromFur
    }

    @Override
    public Animal[] bearYoungFromEggs() {
        return new Platypus[2];
    }
}
