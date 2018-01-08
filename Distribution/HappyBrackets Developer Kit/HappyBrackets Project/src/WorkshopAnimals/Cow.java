package WorkshopAnimals;

public class Cow extends Mammal implements LiveYoung {
    @Override
    Animal[] BearYoung() {
        return  bearLiveYoung();
    }

    @Override
    public Animal[] bearLiveYoung() {
        return new Cow[1];
    }

    @Override
    void nurseBabiesWithMilk() {
        // Baby Drinks Milk From Udder
    }
}
