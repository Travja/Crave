<script>
    export let name;
    export let image = "https://www.seriouseats.com/thmb/3JoYWz3_PajrDhL57P9eQrpg-xE=/735x0/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__recipes__images__2015__07__20150702-sous-vide-hamburger-anova-primary-bf5eefff4505446f9cbf33f5f2d9b2e6.jpg";
    export let price;

    const formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',

        // These options are needed to round to whole numbers if that's what you want.
        //minimumFractionDigits: 0, // (this suffices for whole numbers, but will print 2500.10 as $2,500.1)
        //maximumFractionDigits: 0, // (causes 2500.99 to be printed as $2,501)
    });
</script>

<section>
    <img src={image} alt={name}/>
    <div>{name}</div>
    <div class="price">{formatter.format(price)}</div>
</section>

<style>
    section {
        margin: 20px;
        padding: 20px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: rgba(0, 0, 0, 0.3);
        border-radius: 10px;
        box-shadow: 5px 5px 8px #333;
    }

    img {
        max-width: 200px;
    }

    .price {
        color: #666;
    }
</style>