<script>
    import {findCheapest, formatter} from "$lib/util";
    import {gateway} from "$lib/variables";
    import {createEventDispatcher} from "svelte";

    const dispatch = createEventDispatcher();

    export let name = "No Name",
        upc = "Missing UPC",
        image = "/find-image.svg",
        favorite = false;
    export let details = [];
    export let inShoppingList = false;

    let lowestDetails,
        store,
        href;

    $: href = `/items/${upc}`;
    $: lowestDetails = findCheapest(details);

    const favoriteItem = e => {
        favorite = !favorite;
        fetch(`${gateway()}/item-service/items/${favorite ? "" : "un"}favorite/${upc}`,
            {method: "post"})
            .then(res => res.json())
            .then(json => {
                if (!json.success)
                    favorite = !favorite;
            })
            .catch(e => console.error(e));
    };

    const addToShoppingList = () => {
        dispatch("addtolist", name);
    };
</script>

<div class="section">
    <a alt="{name}" {href}>
        <img alt={name} src={image}/>
        <div class="sp"/>
        <div class="name">{name}</div>
        <div class="price">Lowest price: <strong>{formatter.format(lowestDetails?.price)}</strong></div>
    </a>
    <div class="footer">
        <div class="store">{lowestDetails?.store?.name}</div>
        <div class="upc">{upc}</div>
    </div>
    <div class="icons hcenter">
        <div class="material-icons-round favorite" on:click={favoriteItem}
             title="{favorite ? 'Un-favorite' : 'Favorite'}">{favorite ? "star" : "star_outline"}</div>
        <div class="material-icons-round blank-button" on:click={addToShoppingList}
             title="Add to Shopping List">{inShoppingList ? "playlist_add_check" :
            "playlist_add"}
        </div>
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
        flex: 1;
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

    .name {
        text-align: center;
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
        display: flex;
        flex-direction: row;
    }

    .icons > * {
        margin: 0 0.3em;
        align-items: center;
        display: flex;
    }

    .icons .favorite {
        font-size: 1.8rem;
        user-select: none;
    }

    .icons .favorite:hover {
        cursor: pointer;
    }
</style>