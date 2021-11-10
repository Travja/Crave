<script context="module">
    import {gateway} from "$lib/variables";

    let name = "";

    export async function load({page, fetch}) {
        if (page.query.get("item")) {
            const url = `${gateway()}/item-service/items/name/${page.query.get("item")}`;
            console.log(url);
            const res = await fetch(url);

            if (res.ok) {
                let json = await res.json()
                console.log(json);
                return name;
            }

            console.log(res);
        }
    }
</script>

<script>
    import {page} from "$app/stores";
    import StoreSelector from "$lib/ui/StoreSelector.svelte";
    import {title} from "$lib/variables";

    title.set("Create a Sale");

    let upc = $page.query.get("item");
    let store = $page.query.get("store");

    let selector;

    const submit = () => {
        console.log(selector.getSelectedStore());
    };
</script>

<div class="parent full">
    <div class="wrapper">
        <p>Please enter the sale information for the item and upload an applicable advertisement so we can verify
            that the sale is legitimate!</p>

        <hr/>

        <StoreSelector bind:this={selector} {store}/>

        <label for="upc">UPC</label>
        <input disabled="{!!upc}" id="upc" name="upc" placeholder="UPC" type="number" value="{upc}"/>

        <label for="price">Price</label>
        <input id="price" min="0" name="price" placeholder="Sale Price" step="0.01" type="number"/>

        <label class="button" for="ad">Upload Sale Advertisement</label>
        <input id="ad" name="ad" type="file"/>
        <div class="button publish" on:click={submit}>Publish Sale</div>
    </div>
</div>

<style>
    .parent {
        margin: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
    }

    hr {
        width: 90%;
    }

    .wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .in label {
        flex-grow: 1;
    }

    input[type="file"] {
        display: none;
    }

    .publish {
        background: var(--submit-color);
    }
</style>