<script>
    import {findCheapest, formatter} from "$lib/util";
    import {icons} from "$lib/variables";

    export let name = "No Name",
        upc = "Missing UPC",
        image = "/find-image.svg",
        favorite = false;
    export let details = [];

    let lowestDetails,
        store;

    $: href = `/items/${upc}`;
    $: lowestDetails = findCheapest(details);

    const favoriteItem = e => {
        favorite = !favorite;
        //TODO Send request so backend knows that the item is favorited.
    };
</script>

<div class="section">
    <a alt="{name}" {href}>
        <img alt={name} src={image}/>
        <div class="sp"/>
        <div>{name}</div>
        <div class="price">Lowest price: <strong>{formatter.format(lowestDetails?.price)}</strong></div>
    </a>
    <div class="footer">
        <div class="store">{lowestDetails?.store?.name}</div>
        <div class="upc">{upc}</div>
    </div>
    <div class="icons hcenter">
        <img alt="{favorite ? 'Un-favorite' : 'Favorite'}"
             on:click={favoriteItem}
             src="{favorite ? icons.starFull : icons.starEmpty}"/>
    </div>
</div>

<style>
    .section, a {
        color: var(--fg-color);
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        flex-grow: 1;
    }

    .section {
        padding: 20px;
        border: 1px solid rgba(0, 0, 0, 0.25);
        align-items: stretch;
    }

    .section:hover, a:hover {
        color: var(--fg-color);
        text-decoration: none;
        background-color: #ddd;
    }

    a:visited {
        color: var(--fg-color);
    }

    img {
        min-width: 200px;
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
        margin-right: 1.5em;
    }

    .upc {
        font-size: 0.8em;
        color: #666;
    }

    .icons {
        height: 1.5em;
    }

    .icons img {
        height: 100%;
    }

    .icons img:hover {
        cursor: pointer;
    }
</style>