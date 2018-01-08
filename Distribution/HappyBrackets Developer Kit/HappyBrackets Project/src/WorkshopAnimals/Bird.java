package WorkshopAnimals;

public abstract class Bird extends Animal implements LayEggs
{
    @Override
    Animal[] BearYoung() {
        return bearYoungFromEggs();
    }

    public abstract Bird[] bearYoungFromEggs();
}
