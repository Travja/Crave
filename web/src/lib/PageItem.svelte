<script>
    import {findCheapest, formatter} from "$lib/util";

    export let name = "No Name",
        upc = "Missing UPC",
        image = "https://www.seriouseats.com/thmb/3JoYWz3_PajrDhL57P9eQrpg-xE=/735x0/__opt__aboutcom__coeus__resources__content_migration__serious_eats__seriouseats.com__recipes__images__2015__07__20150702-sous-vide-hamburger-anova-primary-bf5eefff4505446f9cbf33f5f2d9b2e6.jpg";
    export let details = [];

    let lowestDetails,
        store;

    $: lowestDetails = findCheapest(details);
</script>

<a alt="{name}" class="section" href="/items/{upc}">
    <img alt={name} src={image}/>
    <div class="sp"/>
    <div>{name}</div>
    <div class="price">Lowest price: <strong>{formatter.format(lowestDetails?.price)}</strong></div>
    <div class="footer">
        <div class="store">{lowestDetails?.store?.name}</div>
        <div class="upc">{upc}</div>
    </div>
</a>

<style>
    a.section {
        color: var(--fg-color);
        position: relative;
        padding: 20px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    a.section:hover {
        color: var(--fg-color);
        text-decoration: none;
        background-color: #ddd;
    }

    a.section:visited {
        color: var(--fg-color);
    }

    img {
        max-width: 200px;
    }

    .price {
        color: #666;
        margin-top: 10px;
    }

    .footer {
        display: flex;
        width: 100%;
        align-items: flex-end;
    }

    .store {
        color: #666;
        flex-grow: 1;
    }

    .upc {
        font-size: 0.8em;
        color: #666;
    }
</style>