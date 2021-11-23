<script context="module">
    import {gateway, title} from "$lib/variables";
    import {findCheapest} from "$lib/util";

    title.set("Item Entry");

    let src = "/find-image.svg";
    let items;
    let item;

    export async function load({page, fetch}) {
        const url = `${gateway()}/item-service/item-details/${page.params.item}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json();
            console.log(json);
            items = json;
            item = findCheapest(items);
            if (!item.item.description)
                item.item.description = "No description";
            item.item.description = item.item.description.replaceAll("\n", "<br/>");
            console.log(item);
            title.set(item.item.name);
            return item, items;
        }

        console.log(res);
    }
</script>

<script>
    import FullItemPage from "$lib/layouts/FullItemPage.svelte";
    import {onMount} from "svelte";
    import {parseJWT} from "$lib/util";
    import {goto} from "$app/navigation";

    onMount(() => {
        let jwt = parseJWT();
        if (!jwt || !jwt.authorities.includes("ADMIN"))
            goto("/items/" + item.item.upc, {replaceState: true});
    });
    // let imgUpload;
    //
    // const changeImg = e => {
    //     src = URL.createObjectURL(e.target.files[0]);
    // };
</script>

<FullItemPage editable="true" {item} {items} {src}/>